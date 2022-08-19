package io.github.irgaly.kkvs.internal

import kotlinx.serialization.json.Json
import io.github.irgaly.kkvs.KkvsEntry
import io.github.irgaly.kkvs.KkvsStorage
import io.github.irgaly.kkvs.KkvsStorageOptions
import io.github.irgaly.kkvs.internal.encoder.Encoder
import io.github.irgaly.kkvs.internal.model.Item
import io.github.irgaly.kkvs.internal.model.ItemEvent
import io.github.irgaly.kkvs.internal.model.ItemEventType
import io.github.irgaly.kkvs.internal.strategy.KkvsStrategyOperatorImpl
import io.github.irgaly.kkvs.platform.KkvsPlatformCalendar
import io.github.irgaly.kkvs.strategy.KkvsStrategy
import kotlin.reflect.KType
import kotlin.time.Duration

internal class KkvsStorageImpl(
    val name: String,
    json: Json,
    val options: KkvsStorageOptions,
    val databaseManager: KkvsDatabaseManager,
    val calendar: KkvsPlatformCalendar
) : KkvsStorage {
    private val encoder = Encoder(json)

    private val strategy: KkvsStrategy = options.strategy

    private val itemRepository by lazy {
        databaseManager.getItemRepository(name)
    }

    private val itemEventRepository by lazy {
        databaseManager.itemEventRepository
    }

    init {
        strategy.initialize(
            KkvsStrategyOperatorImpl(
                databaseManager,
                name
            )
        )
    }

    override val defaultExpireTime: Duration? get() = options.defaultExpireTime

    override suspend fun <T : Any> get(key: String, type: KType): T {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = databaseManager.transactionWithResult {
            var item = itemRepository.get(key)
            if (item != null) {
                if (item.isAvailable(now)) {
                    strategy.onItemRead(key, now)
                } else {
                    // delete cache
                    item = null
                    itemRepository.delete(key)
                    itemRepository.decrementStatsCount(1)
                    itemEventRepository.create(
                        ItemEvent(
                            createdAt = now,
                            itemType = name,
                            itemKey = key,
                            eventType = ItemEventType.Expired
                        )
                    )
                }
            }
            item
        } ?: throw NoSuchElementException("key = $key, storage name = $name")
        return encoder.decode(item, type)
    }

    override suspend fun <T : Any> getOrNull(key: String, type: KType): T? {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = databaseManager.transactionWithResult {
            var item = itemRepository.get(key)
            if (item != null) {
                if (item.isAvailable(now)) {
                    strategy.onItemRead(key, now)
                } else {
                    // delete cache
                    item = null
                    itemRepository.delete(key)
                    itemRepository.decrementStatsCount(1)
                    itemEventRepository.create(
                        ItemEvent(
                            createdAt = now,
                            itemType = name,
                            itemKey = key,
                            eventType = ItemEventType.Expired
                        )
                    )
                }
            }
            item
        }
        return item?.let { encoder.decode(it, type) }
    }

    override suspend fun <T : Any> read(key: String, type: KType): KkvsEntry<T> {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = databaseManager.transactionWithResult {
            var item = itemRepository.get(key)
            if (item != null) {
                if (item.isAvailable(now)) {
                    itemRepository.updateLastRead(key, now)
                } else {
                    // delete cache
                    item = null
                    itemRepository.delete(key)
                    itemRepository.decrementStatsCount(1)
                    itemEventRepository.create(
                        ItemEvent(
                            createdAt = now,
                            itemType = name,
                            itemKey = key,
                            eventType = ItemEventType.Expired
                        )
                    )
                }
            }
            item
        } ?: throw NoSuchElementException("key = $key, storage name = $name")
        return KkvsEntry(
            item,
            type,
            encoder
        )
    }

    override suspend fun contains(key: String): Boolean {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = itemRepository.get(key)
        return (item?.isAvailable(now) ?: false)
    }

    override suspend fun <T : Any> put(key: String, value: T, type: KType) {
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
            val isCreate = !itemRepository.exists(key)
            itemRepository.upsert(item)
            if (isCreate) {
                itemRepository.incrementStatsCount(1)
            }
            itemEventRepository.create(
                ItemEvent(
                    createdAt = now,
                    itemType = name,
                    itemKey = key,
                    eventType = ItemEventType.Create
                )
            )
            if (isCreate) {
                val count = itemRepository.getStatsCount()
                strategy.onPostItemCreate(key, count, now)
            }
        }
    }

    override suspend fun remove(key: String): Boolean {
        val now = calendar.nowUtcEpochTimeMillis()
        return databaseManager.transactionWithResult {
            val exists = itemRepository.exists(key)
            if (exists) {
                itemRepository.delete(key)
                itemRepository.decrementStatsCount(1)
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

    override suspend fun removeAll(key: String) {
        val now = calendar.nowUtcEpochTimeMillis()
        databaseManager.transaction {
            itemRepository.getAllKeys { key ->
                itemEventRepository.create(
                    ItemEvent(
                        createdAt = now,
                        itemType = name,
                        itemKey = key,
                        eventType = ItemEventType.Delete
                    )
                )
            }
            itemRepository.deleteAll()
            itemRepository.updateStatsCount(0)
        }
    }

    override suspend fun clean() {
        TODO("Not yet implemented")
    }

    override suspend fun clear() {
        databaseManager.transaction {
            itemRepository.deleteAll()
            itemRepository.deleteStats()
        }
    }
}