package io.github.irgaly.kottage

import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * List operations
 *
 * Item List is implemented by Linked List in Database.
 */
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
    suspend fun <T : Any> getPageFrom(
        positionId: String?,
        pageSize: Long?,
        type: KType,
        direction: KottageListDirection = KottageListDirection.Forward
    ): KottageListPage<T>

    suspend fun getSize(): Long
    suspend fun <T : Any> getFirst(type: KType): KottageListItem<T>?
    suspend fun <T : Any> getLast(type: KType): KottageListItem<T>?

    suspend fun <T : Any> get(positionId: String, type: KType): KottageListItem<T>?

    /**
     * Get item with iteration.
     */
    @Throws(
        IndexOutOfBoundsException::class,
        CancellationException::class
    )
    suspend fun <T : Any> getByIndex(
        index: Long,
        type: KType,
        direction: KottageListDirection = KottageListDirection.Forward
    ): KottageListItem<T>?

    suspend fun <T : Any> add(
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData? = null
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun addKey(key: String, metaData: KottageListMetaData? = null)
    suspend fun <T : Any> addAll(values: List<KottageListEntry<T>>, type: KType)

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
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun addKeyFirst(key: String, metaData: KottageListMetaData? = null)
    suspend fun <T : Any> addAllFirst(values: List<KottageListEntry<T>>, type: KType)

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun addKeysFirst(keys: List<String>, metaData: KottageListMetaData? = null)

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun <T : Any> update(positionId: String, key: String, value: T, type: KType)

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun updateKey(positionId: String, key: String)

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
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertKeyAfter(
        positionId: String,
        key: String,
        metaData: KottageListMetaData? = null
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun <T : Any> insertAllAfter(
        positionId: String,
        values: List<KottageListEntry<T>>,
        type: KType
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
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun insertKeyBefore(
        positionId: String,
        key: String,
        metaData: KottageListMetaData? = null
    )

    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun <T : Any> insertAllBefore(
        positionId: String,
        values: List<KottageListEntry<T>>,
        type: KType
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
}

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.getPageFrom(
    positionId: String?,
    pageSize: Long?,
    direction: KottageListDirection = KottageListDirection.Forward
): KottageListPage<T> = getPageFrom(
    positionId = positionId,
    pageSize = pageSize,
    type = typeOf<T>(),
    direction = direction
)

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.getFirst(): KottageListItem<T>? =
    getFirst(type = typeOf<T>())

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.getLast(): KottageListItem<T>? =
    getLast(type = typeOf<T>())

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.get(positionId: String): KottageListItem<T>? =
    get(positionId = positionId, type = typeOf<T>())

/**
 * Get item with iteration.
 */
@Suppress("unused")
@Throws(
    IndexOutOfBoundsException::class,
    CancellationException::class
)
suspend inline fun <reified T : Any> KottageList.getByIndex(
    index: Long,
    direction: KottageListDirection = KottageListDirection.Forward
): KottageListItem<T>? = getByIndex(index = index, type = typeOf<T>(), direction = direction)

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.add(
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
) = add(key = key, value = value, type = typeOf<T>(), metaData = metaData)

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.addAll(
    values: List<KottageListEntry<T>>
) = addAll(values = values, type = typeOf<T>())

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.addFirst(
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
) = addFirst(key = key, value = value, type = typeOf<T>(), metaData = metaData)

@Suppress("unused")
suspend inline fun <reified T : Any> KottageList.addAllFirst(
    values: List<KottageListEntry<T>>
) = addAllFirst(values = values, type = typeOf<T>())

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
suspend inline fun <reified T : Any> KottageList.insertAllAfter(
    positionId: String,
    values: List<KottageListEntry<T>>,
) = insertAllAfter(positionId = positionId, values = values, type = typeOf<T>())

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

@Suppress("unused")
@Throws(
    NoSuchElementException::class,
    CancellationException::class
)
suspend inline fun <reified T : Any> KottageList.insertAllBefore(
    positionId: String,
    values: List<KottageListEntry<T>>
) = insertAllBefore(positionId = positionId, values = values, type = typeOf<T>())
