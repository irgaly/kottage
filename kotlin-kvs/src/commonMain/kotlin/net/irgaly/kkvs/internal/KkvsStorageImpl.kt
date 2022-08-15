package net.irgaly.kkvs.internal

import net.irgaly.kkvs.KkvsEntry
import net.irgaly.kkvs.KkvsStorage
import net.irgaly.kkvs.KkvsStorageOptions
import net.irgaly.kkvs.internal.model.ItemEvent
import net.irgaly.kkvs.internal.model.ItemEventType
import net.irgaly.kkvs.internal.repository.KkvsRepositoryFactory
import net.irgaly.kkvs.platform.KkvsPlatformCalendar
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class KkvsStorageImpl(
    val name: String,
    val options: KkvsStorageOptions,
    val repositoryFactory: KkvsRepositoryFactory,
    val calendar: KkvsPlatformCalendar
) : KkvsStorage {
    private val itemRepository by lazy {
        repositoryFactory.createItemRepository(name)
    }

    private val itemEventRepository by lazy {
        repositoryFactory.createItemEventRepository()
    }

    override val defaultExpireTime: Duration? get() = options.defaultExpireTime

    override suspend fun <T : Any> get(key: String, type: KClass<T>): T {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> getOrNull(key: String, type: KClass<T>): T? {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> read(key: String, type: KClass<T>): KkvsEntry<T> {
        TODO("Not yet implemented")
    }

    override suspend fun contains(key: String): Boolean {
        val now = calendar.nowUtcEpochTimeMillis()
        val item = itemRepository.get(key)
        return (item?.isAvailable(now) ?: false)
    }

    override suspend fun <T : Any> put(key: String, value: T) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(key: String): Boolean {
        return repositoryFactory.transactionWithResult {
            val exists = itemRepository.exists(key)
            if (exists) {
                val now = calendar.nowUtcEpochTimeMillis()
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
