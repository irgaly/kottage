package net.irgaly.kkvs.internal

import net.irgaly.kkvs.KkvsEntry
import net.irgaly.kkvs.KkvsStorage
import net.irgaly.kkvs.KkvsStorageOptions
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class KkvsStorageImpl(
    val name: String,
    val options: KkvsStorageOptions,
    val repository: KkvsRepository
): KkvsStorage {
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
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> put(key: String, value: T) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(key: String): Boolean {
        TODO("Not yet implemented")
    }
}
