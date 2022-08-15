package net.irgaly.kkvs.internal

import kotlinx.serialization.json.Json
import net.irgaly.kkvs.KkvsEntry
import net.irgaly.kkvs.KkvsStorage
import net.irgaly.kkvs.KkvsStorageOptions
import net.irgaly.kkvs.internal.encoder.Encoder
import net.irgaly.kkvs.internal.model.Item
import net.irgaly.kkvs.internal.model.ItemEvent
import net.irgaly.kkvs.internal.model.ItemEventType
import net.irgaly.kkvs.internal.repository.KkvsRepositoryFactory
import net.irgaly.kkvs.platform.KkvsPlatformCalendar
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class KkvsStorageImpl(
    val name: String,
    json: Json,
    val options: KkvsStorageOptions,
    val repositoryFactory: KkvsRepositoryFactory,
    val calendar: KkvsPlatformCalendar
) : KkvsStorage {
    private val encoder = Encoder(json)

    private val itemRepository by lazy {
        repositoryFactory.createItemRepository(name)
    }

    private val itemEventRepository by lazy {
        repositoryFactory.createItemEventRepository()
    }

    override val defaultExpireTime: Duration? get() = options.defaultExpireTime

    override suspend fun <T : Any> get(key: String, type: KType): T {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = repositoryFactory.transactionWithResult {
            var item = itemRepository.get(key)
            if (item?.isExpired(now) == true) {
                // delete cache
                item = null
                itemRepository.delete(key)
                itemEventRepository.create(
                    ItemEvent(
                        createdAt = now,
                        itemType = name,
                        itemKey = key,
                        eventType = ItemEventType.Expired
                    )
                )
            }
            item
        } ?: throw NoSuchElementException("key = $key, storage name = $name")
        return encoder.decode(item, type)
    }

    override suspend fun <T : Any> getOrNull(key: String, type: KType): T? {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = repositoryFactory.transactionWithResult {
            var item = itemRepository.get(key)
            if (item?.isExpired(now) == true) {
                // delete cache
                item = null
                itemRepository.delete(key)
                itemEventRepository.create(
                    ItemEvent(
                        createdAt = now,
                        itemType = name,
                        itemKey = key,
                        eventType = ItemEventType.Expired
                    )
                )
            }
            item
        }
        return item?.let { encoder.decode(it, type) }
    }

    override suspend fun <T : Any> read(key: String, type: KClass<T>): KkvsEntry<T> {
        TODO("Not yet implemented")
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
        repositoryFactory.transaction {
            itemRepository.upsert(item)
            itemEventRepository.create(
                ItemEvent(
                    createdAt = now,
                    itemType = name,
                    itemKey = key,
                    eventType = ItemEventType.Create
                )
            )
        }
    }

    override suspend fun remove(key: String): Boolean {
        val now = calendar.nowUtcEpochTimeMillis()
        return repositoryFactory.transactionWithResult {
            val exists = itemRepository.exists(key)
            if (exists) {
                itemRepository.delete(key)
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
}
