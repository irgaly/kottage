package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEntry
import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.KottageStorageOptions
import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.strategy.KottageStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.reflect.KType
import kotlin.time.Duration

internal class KottageStorageImpl(
    private val name: String,
    json: Json,
    private val options: KottageStorageOptions,
    private val kottageOptions: KottageOptions,
    private val databaseManager: KottageDatabaseManager,
    private val calendar: KottageCalendar,
    private val onCompactionRequired: suspend () -> Unit,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : KottageStorage {
    private val encoder = Encoder(json)

    private val strategy: KottageStrategy = options.strategy

    private suspend fun itemRepository() = databaseManager.itemRepository.await()
    private suspend fun itemEventRepository() = databaseManager.itemEventRepository.await()
    private suspend fun statsRepository() = databaseManager.statsRepository.await()
    private suspend fun operator() = databaseManager.operator.await()

    override val defaultExpireTime: Duration? get() = options.defaultExpireTime

    override suspend fun <T : Any> get(key: String, type: KType): T {
        return getOrNullInternal(key, type)
            ?: throw NoSuchElementException("key = $key, storage name = $name")
    }

    override suspend fun <T : Any> getOrNull(key: String, type: KType): T? {
        return getOrNullInternal(key, type)
    }

    /**
     * @throws ClassCastException when decode failed
     * @throws SerializationException when json decode failed
     */
    private suspend fun <T : Any> getOrNullInternal(key: String, type: KType): T? =
        withContext(dispatcher) {
            val operator = operator()
            val now = calendar.nowUnixTimeMillis()
            var compactionRequired = false
            val item = databaseManager.transactionWithResult {
                compactionRequired =
                    operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
                operator.getOrNull(key, name, now)?.also {
                    strategy.onItemRead(key, name, now, operator)
                }
            }
            if (compactionRequired) {
                onCompactionRequired()
            }
            item?.let { encoder.decode(it, type) }
        }

    override suspend fun <T : Any> getEntry(key: String, type: KType): KottageEntry<T> {
        return getEntryOrNullInternal(key, type)
            ?: throw NoSuchElementException("key = $key, storage name = $name")
    }

    override suspend fun <T : Any> getEntryOrNull(key: String, type: KType): KottageEntry<T>? {
        return getEntryOrNullInternal(key, type)
    }

    private suspend fun <T : Any> getEntryOrNullInternal(
        key: String,
        type: KType
    ): KottageEntry<T>? = withContext(dispatcher) {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        var compactionRequired = false
        val item = databaseManager.transactionWithResult {
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
            operator.getOrNull(key, name, now)?.also {
                strategy.onItemRead(key, name, now, operator)
            }
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        item?.let { KottageEntry(it, type, encoder) }
    }

    override suspend fun exists(key: String): Boolean = withContext(dispatcher) {
        val now = calendar.nowUnixTimeMillis()
        val item = itemRepository().get(key, name)
        (item?.isAvailable(now) ?: false)
    }

    override suspend fun <T : Any> put(key: String, value: T, type: KType) =
        withContext(dispatcher) {
            val itemRepository = itemRepository()
            val itemEventRepository = itemEventRepository()
            val operator = operator()
            val now = calendar.nowUnixTimeMillis()
            val item = encoder.encode(
                value,
                type
            ) { stringValue: String?,
                longValue: Long?,
                doubleValue: Double?,
                bytesValue: ByteArray? ->
                Item(
                    key = key,
                    type = name,
                    stringValue = stringValue,
                    longValue = longValue,
                    doubleValue = doubleValue,
                    bytesValue = bytesValue,
                    createdAt = now,
                    lastReadAt = now,
                    expireAt = defaultExpireTime?.inWholeMilliseconds?.let { duration ->
                        now + duration
                    }
                )
            }
            var compactionRequired = false
            databaseManager.transaction {
                val isCreate = !itemRepository.exists(key, name)
                itemRepository.upsert(item)
                if (isCreate) {
                    itemRepository.incrementStatsCount(name, 1)
                }
                itemEventRepository.create(
                    ItemEvent(
                        createdAt = now,
                        itemType = name,
                        itemKey = key,
                        eventType = if (isCreate) ItemEventType.Create else ItemEventType.Update
                    )
                )
                if (isCreate) {
                    val count = itemRepository.getStatsCount(name)
                    strategy.onPostItemCreate(key, name, count, now, operator)
                }
                compactionRequired =
                    operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
            }
            if (compactionRequired) {
                onCompactionRequired()
            }
        }

    override suspend fun remove(key: String): Boolean = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val itemEventRepository = itemEventRepository()
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        var compactionRequired = false
        val exists = databaseManager.transactionWithResult {
            val exists = itemRepository.exists(key, name)
            if (exists) {
                itemRepository.delete(key, name)
                itemRepository.decrementStatsCount(name, 1)
                itemEventRepository.create(
                    ItemEvent(
                        createdAt = now,
                        itemType = name,
                        itemKey = key,
                        eventType = ItemEventType.Delete
                    )
                )
            }
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
            exists
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        exists
    }

    override suspend fun removeAll(key: String) = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val itemEventRepository = itemEventRepository()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
            itemRepository.getAllKeys(name) { key ->
                itemEventRepository.create(
                    ItemEvent(
                        createdAt = now,
                        itemType = name,
                        itemKey = key,
                        eventType = ItemEventType.Delete
                    )
                )
            }
            itemRepository.deleteAll(name)
            itemRepository.updateStatsCount(name, 0)
        }
    }

    override suspend fun compact() = withContext(dispatcher) {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
            operator.evictCache(now, name)
        }
    }

    override suspend fun clear() = withContext(dispatcher) {
        val itemRepository = itemRepository()
        databaseManager.transaction {
            itemRepository.deleteAll(name)
            itemRepository.deleteStats(name)
        }
    }
}
