package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEntry
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
    private val databaseManager: KottageDatabaseManager,
    private val calendar: KottageCalendar,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : KottageStorage {
    private val encoder = Encoder(json)

    private val strategy: KottageStrategy = options.strategy

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
            val now = calendar.nowUtcEpochTimeMillis()
            databaseManager.transactionWithResult {
                operator.getOrNull(key, name, now)?.also {
                    strategy.onItemRead(key, name, now, operator)
                }
            }?.let { encoder.decode(it, type) }
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
        val now = calendar.nowUtcEpochTimeMillis()
        databaseManager.transactionWithResult {
            operator.getOrNull(key, name, now)?.also {
                strategy.onItemRead(key, name, now, operator)
            }
        }?.let {
            KottageEntry(it, type, encoder)
        }
    }

    override suspend fun contains(key: String): Boolean = withContext(dispatcher) {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = itemRepository().get(key, name)
        (item?.isAvailable(now) ?: false)
    }

    override suspend fun <T : Any> put(key: String, value: T, type: KType) =
        withContext(dispatcher) {
            val itemRepository = itemRepository()
            val itemEventRepository = itemEventRepository()
            val operator = operator()
            val now = calendar.nowUtcEpochTimeMillis()
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
            }
        }

    override suspend fun remove(key: String): Boolean = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val itemEventRepository = itemEventRepository()
        val now = calendar.nowUtcEpochTimeMillis()
        databaseManager.transactionWithResult {
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
            exists
        }
    }

    override suspend fun removeAll(key: String) = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val itemEventRepository = itemEventRepository()
        val now = calendar.nowUtcEpochTimeMillis()
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
        val now = calendar.nowUtcEpochTimeMillis()
        databaseManager.transaction {
            operator.compact(name, now)
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
