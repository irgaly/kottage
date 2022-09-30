package io.github.irgaly.kottage

import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration

interface KottageStorage {
    val name: String
    val defaultExpireTime: Duration?

    /**
     * @throws NoSuchElementException when key does not exist
     * @throws ClassCastException when decode failed
     * @throws SerializationException when json decode failed
     */
    @Throws(
        NoSuchElementException::class,
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class,
    )
    suspend fun <T : Any> get(key: String, type: KType): T

    /**
     * @throws ClassCastException when decode failed
     * @throws SerializationException when json decode failed
     */
    @Throws(
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class,
    )
    suspend fun <T : Any> getOrNull(key: String, type: KType): T?

    /**
     * @throws NoSuchElementException when key does not exist
     */
    @Throws(
        NoSuchElementException::class,
        CancellationException::class,
    )
    suspend fun <T : Any> getEntry(key: String, type: KType): KottageEntry<T>

    @Throws(CancellationException::class)
    suspend fun <T : Any> getEntryOrNull(key: String, type: KType): KottageEntry<T>?

    @Throws(CancellationException::class)
    suspend fun exists(key: String): Boolean

    /**
     * @throws SerializationException when json encoding failed
     */
    @Throws(
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class
    )
    suspend fun <T : Any> put(key: String, value: T, type: KType, expireTime: Duration? = null)

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

    /**
     * Get events after fromUnitTimeMillisAt
     */
    suspend fun getEvents(afterUnixTimeMillisAt: Long, limit: Long? = null): List<KottageEvent>

    /**
     * get KottageEventFlow
     */
    fun eventFlow(afterUnixTimeMillisAt: Long? = null): KottageEventFlow

    /**
     * get KottageList
     */
    fun list(
        name: String,
        optionsBuilder: (KottageListOptions.Builder.() -> Unit)? = null
    ): KottageList
}

/**
 * @throws NoSuchElementException when key does not exist
 * @throws ClassCastException when decode failed
 * @throws SerializationException when json decode failed
 */
@Throws(
    NoSuchElementException::class,
    ClassCastException::class,
    SerializationException::class,
    CancellationException::class,
)
suspend inline fun <reified T : Any> KottageStorage.get(key: String): T {
    return get(key, typeOf<T>())
}

/**
 * @throws ClassCastException when decode failed
 * @throws SerializationException when json decode failed
 */
@Throws(
    ClassCastException::class,
    SerializationException::class,
    CancellationException::class,
)
suspend inline fun <reified T : Any> KottageStorage.getOrNull(key: String): T? {
    return getOrNull(key, typeOf<T>())
}

@Throws(
    ClassCastException::class,
    SerializationException::class,
    CancellationException::class
)
suspend inline fun <reified T : Any> KottageStorage.put(
    key: String,
    value: T,
    expireTime: Duration? = null
) {
    put(key, value, typeOf<T>(), expireTime)
}

@Throws(
    NoSuchElementException::class,
    CancellationException::class,
)
suspend inline fun <reified T : Any> KottageStorage.getEntry(key: String): KottageEntry<T> {
    return getEntry(key, typeOf<T>())
}

@Throws(CancellationException::class)
suspend inline fun <reified T : Any> KottageStorage.getEntryOrNull(key: String): KottageEntry<T>? {
    return getEntryOrNull(key, typeOf<T>())
}
