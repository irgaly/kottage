package io.github.irgaly.kottage

import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration

interface KottageStorage {
    val defaultExpireTime: Duration?

    @Throws(
        NoSuchElementException::class,
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class,
    )
    suspend fun <T : Any> get(key: String, type: KType): T

    @Throws(
        NoSuchElementException::class,
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class,
    )
    suspend fun <T : Any> getOrNull(key: String, type: KType): T?

    @Throws(
        NoSuchElementException::class,
        CancellationException::class,
    )
    suspend fun <T : Any> read(key: String, type: KType): KottageEntry<T>
    suspend fun contains(key: String): Boolean

    @Throws(
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class
    )
    suspend fun <T : Any> put(key: String, value: T, type: KType)
    suspend fun remove(key: String): Boolean

    /**
     * Delete all entries
     */
    suspend fun removeAll(key: String)

    /**
     * Clean Expired entries
     */
    suspend fun compact()

    /**
     * Delete all entries and events
     */
    suspend fun clear()
}

suspend inline fun <reified T : Any> KottageStorage.get(key: String): T {
    return get(key, typeOf<T>())
}

suspend inline fun <reified T : Any> KottageStorage.put(key: String, value: T) {
    put(key, value, typeOf<T>())
}

suspend inline fun <reified T : Any> KottageStorage.read(key: String): KottageEntry<T> {
    return read(key, typeOf<T>())
}
