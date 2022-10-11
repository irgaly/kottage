package io.github.irgaly.kottage

import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * List operations
 *
 * Item List is implemented by Linked List in Database.
 */
@Suppress("unused")
interface KottageList {
    val name: String
    val storage: KottageStorage
    val options: KottageListOptions

    /**
     * @param positionId positionId of first item.
     *                   if positionId is null:
     *                       direction = Forward: get First Page
     *                       direction = Backward: get Last Page
     */
    suspend fun getPageFrom(
        positionId: String?,
        pageSize: Long?,
        direction: KottageListDirection = KottageListDirection.Forward
    ): KottageListPage

    suspend fun getSize(): Long
    suspend fun isEmpty(): Boolean
    suspend fun isNotEmpty(): Boolean
    suspend fun getFirst(): KottageListEntry?
    suspend fun getLast(): KottageListEntry?

    suspend fun get(positionId: String): KottageListEntry?

    /**
     * Get item with iteration.
     */
    @Throws(
        IndexOutOfBoundsException::class,
        CancellationException::class
    )
    suspend fun getByIndex(
        index: Long,
        direction: KottageListDirection = KottageListDirection.Forward
    ): KottageListEntry?

    suspend fun <T : Any> add(
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData? = null
    ): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun addKey(key: String, metaData: KottageListMetaData? = null): KottageListEntry
    suspend fun addAll(values: List<KottageListValue<*>>)

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun addKeys(keys: List<String>, metaData: KottageListMetaData? = null)
    suspend fun <T : Any> addFirst(
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData? = null
    ): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun addKeyFirst(key: String, metaData: KottageListMetaData? = null): KottageListEntry
    suspend fun addAllFirst(values: List<KottageListValue<*>>)

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun addKeysFirst(keys: List<String>, metaData: KottageListMetaData? = null)

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun <T : Any> update(
        positionId: String,
        key: String,
        value: T,
        type: KType
    ): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun updateKey(positionId: String, key: String): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun <T : Any> insertAfter(
        positionId: String,
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData? = null
    ): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertKeyAfter(
        positionId: String,
        key: String,
        metaData: KottageListMetaData? = null
    ): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertAllAfter(
        positionId: String,
        values: List<KottageListValue<*>>
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertKeysAfter(
        positionId: String,
        keys: List<String>,
        metaData: KottageListMetaData? = null
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun <T : Any> insertBefore(
        positionId: String,
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData? = null
    ): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertKeyBefore(
        positionId: String,
        key: String,
        metaData: KottageListMetaData? = null
    ): KottageListEntry

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertAllBefore(
        positionId: String,
        values: List<KottageListValue<*>>
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertKeysBefore(
        positionId: String,
        keys: List<String>,
        metaData: KottageListMetaData? = null
    )

    suspend fun remove(positionId: String)

    suspend fun compact()

    suspend fun clear()

    suspend fun getDebugStatus(): String

    suspend fun getDebugListRawData(): String
}

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.add(
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
) = add(key = key, value = value, type = typeOf<T>(), metaData = metaData)

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.addFirst(
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
) = addFirst(key = key, value = value, type = typeOf<T>(), metaData = metaData)

@Suppress("unused")
@Throws(
    NoSuchElementException::class,
    CancellationException::class
)
suspend inline fun <reified T : Any> KottageList.update(
    positionId: String,
    key: String,
    value: T,
) = update(positionId = positionId, key = key, value = value, type = typeOf<T>())

@Suppress("unused")
@Throws(
    NoSuchElementException::class,
    CancellationException::class
)
suspend inline fun <reified T : Any> KottageList.insertAfter(
    positionId: String,
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
) = insertAfter(
    positionId = positionId,
    key = key,
    value = value,
    type = typeOf<T>(),
    metaData = metaData
)

@Suppress("unused")
@Throws(
    NoSuchElementException::class,
    CancellationException::class
)
suspend inline fun <reified T : Any> KottageList.insertBefore(
    positionId: String,
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
) = insertBefore(
    positionId = positionId,
    key = key,
    value = value,
    type = typeOf<T>(),
    metaData = metaData
)
