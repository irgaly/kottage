package net.irgaly.kkvs

import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
interface KkvsStorage {
    val defaultExpireTime: Duration?
    suspend fun <T: Any> get(key: String, type: KClass<T>): T
    suspend fun <T: Any> getOrNull(key: String, type: KClass<T>): T?
    suspend fun <T: Any> read(key: String, type: KClass<T>): KkvsEntry<T>
    suspend fun contains(key: String): Boolean
    suspend fun <T: Any> put(key: String, value: T)
    suspend fun remove(key: String): Boolean
}
