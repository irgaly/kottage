package io.github.irgaly.kottage

import io.github.irgaly.kottage.property.KottageStore
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration

interface KottageStorage {
    val name: String
    val options: KottageStorageOptions
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
     * @throws ClassCastException when decode failed
     * @throws SerializationException when json decode / decode failed
     */
    @Throws(
        ClassCastException::class,
        SerializationException::class,
        CancellationException::class,
    )
    suspend fun <T : Any> getOrPut(
        key: String,
        type: KType,
        defaultValue: () -> T,
        defaultValueExpireTime: Duration? = null
    ): T

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
    suspend fun removeAll()

    /**
     * Clean Expired entries
     */
    suspend fun compact()

    /**
     * Delete all items and metadata of this storage.
     * This is a clean up operation, so it does not trigger any DELETE events.
     *
     * Deletes:
     * * This storage's items
     * * This storage's metadata
     */
    suspend fun dropStorage()

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
     *
     * @param name A list name. This is a global name in the kottage database, so this must be unique in **this kottage database** (not in this KottageStorage instance).
     */
    fun list(
        name: String,
        optionsBuilder: (KottageListOptions.Builder.() -> Unit)? = null
    ): KottageList

    fun <T : Any> property(
        type: KType,
        key: String? = null,
        expireTime: Duration? = null,
        defaultValue: () -> T
    ): ReadOnlyProperty<Any?, KottageStore<T>>

    fun <T : Any> nullableProperty(
        type: KType,
        key: String? = null,
        expireTime: Duration? = null
    ): ReadOnlyProperty<Any?, KottageStore<T?>>

    suspend fun getDebugStatus(): String
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

/**
 * @throws ClassCastException when decode failed
 * @throws SerializationException when json decode / encode failed
 */
@Throws(
    ClassCastException::class,
    SerializationException::class,
    CancellationException::class,
)
suspend inline fun <reified T : Any> KottageStorage.getOrPut(
    key: String,
    noinline defaultValue: () -> T,
    defaultValueExpireTime: Duration? = null
): T {
    return getOrPut(key, typeOf<T>(), defaultValue, defaultValueExpireTime)
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

inline fun <reified T : Any> KottageStorage.property(
    key: String? = null,
    expireTime: Duration? = null,
    noinline defaultValue: () -> T
): ReadOnlyProperty<Any?, KottageStore<T>> {
    return property(typeOf<T>(), key, expireTime, defaultValue)
}

inline fun <reified T : Any> KottageStorage.nullableProperty(
    key: String? = null,
    expireTime: Duration? = null
): ReadOnlyProperty<Any?, KottageStore<T?>> {
    return nullableProperty(typeOf<T>(), key, expireTime)
}
