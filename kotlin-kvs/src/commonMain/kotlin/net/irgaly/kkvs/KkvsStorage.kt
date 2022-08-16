package net.irgaly.kkvs

import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration

interface KkvsStorage {
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
    suspend fun <T : Any> read(key: String, type: KType): KkvsEntry<T>
    suspend fun contains(key: String): Boolean

    @Throws(
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class
    )
    suspend fun <T : Any> put(key: String, value: T, type: KType)
    suspend fun remove(key: String): Boolean
}

suspend inline fun <reified T : Any> KkvsStorage.get(key: String): T {
    return get(key, typeOf<T>())
}

suspend inline fun <reified T : Any> KkvsStorage.put(key: String, value: T) {
    put(key, value, typeOf<T>())
}

suspend inline fun <reified T : Any> KkvsStorage.read(key: String): KkvsEntry<T> {
    return read(key, typeOf<T>())
}
