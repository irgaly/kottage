package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.*
import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.model.Item
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

    private val itemType: String = name

    private suspend fun itemRepository() = databaseManager.itemRepository.await()
    private suspend fun itemEventRepository() = databaseManager.itemEventRepository.await()
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
                operator.getOrNull(key, itemType, now)?.also {
                    strategy.onItemRead(key, itemType, now, operator)
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
            operator.getOrNull(key, itemType, now)?.also {
                strategy.onItemRead(key, itemType, now, operator)
            }
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        item?.let { KottageEntry(it, type, encoder) }
    }

    override suspend fun exists(key: String): Boolean = withContext(dispatcher) {
        val now = calendar.nowUnixTimeMillis()
        val item = itemRepository().get(key, itemType)
        (item?.isAvailable(now) ?: false)
    }

    override suspend fun <T : Any> put(key: String, value: T, type: KType) =
        withContext(dispatcher) {
            val itemRepository = itemRepository()
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
                    type = itemType,
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
                val isCreate = !itemRepository.exists(key, itemType)
                itemRepository.upsert(item)
                if (isCreate) {
                    itemRepository.incrementStatsCount(itemType, 1)
                    operator.addCreateEvent(
                        now = now,
                        itemType = itemType,
                        itemKey = key,
                        maxEventEntryCount = options.maxEventEntryCount
                    )
                    val count = itemRepository.getStatsCount(itemType)
                    strategy.onPostItemCreate(key, itemType, count, now, operator)
                } else {
                    operator.addUpdateEvent(
                        now = now,
                        itemType = itemType,
                        itemKey = key,
                        maxEventEntryCount = options.maxEventEntryCount
                    )
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
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        var compactionRequired = false
        val exists = databaseManager.transactionWithResult {
            val exists = itemRepository.exists(key, itemType)
            if (exists) {
                itemRepository.delete(key, itemType)
                itemRepository.decrementStatsCount(itemType, 1)
                operator.addDeleteEvent(
                    now = now,
                    itemType = itemType,
                    itemKey = key,
                    maxEventEntryCount = options.maxEventEntryCount
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
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
            itemRepository.getAllKeys(itemType) { key ->
                operator.addDeleteEvent(
                    now = now,
                    itemType = itemType,
                    itemKey = key,
                    maxEventEntryCount = options.maxEventEntryCount
                )
            }
            itemRepository.deleteAll(itemType)
            itemRepository.updateStatsCount(itemType, 0)
        }
    }

    override suspend fun compact() = withContext(dispatcher) {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
            operator.evictCache(now, itemType)
        }
    }

    override suspend fun clear() = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val itemEventRepository = itemEventRepository()
        databaseManager.transaction {
            itemRepository.deleteAll(itemType)
            itemEventRepository.deleteAll(itemType)
            itemRepository.deleteStats(itemType)
        }
    }

    override suspend fun getEvents(afterUnixTimeMillisAt: Long, limit: Long?): List<KottageEvent> {
        val operator = operator()
        return databaseManager.transactionWithResult {
            operator.getEvents(
                itemType = itemType,
                afterUnixTimeMillisAt = afterUnixTimeMillisAt,
                limit = limit
            )
        }
    }
}
