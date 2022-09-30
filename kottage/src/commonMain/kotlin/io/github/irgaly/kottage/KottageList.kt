package io.github.irgaly.kottage

import kotlin.coroutines.cancellation.CancellationException

/**
 * List operations
 *
 * Item List is implemented by Linked List in Database.
 */
interface KottageList {
    val storage: KottageStorage

    /**
     * @param positionId positionId of first item.
     *                   if positionId is null:
     *                       direction = Forward: get First Page
     *                       direction = Backward: get Last Page
     */
    suspend fun <T: Any> getPageFrom(
        positionId: String?,
        pageSize: Long?,
        direction: KottageListDirection = KottageListDirection.Forward): KottageListPage<T>

    suspend fun getSize(): Long
    suspend fun <T: Any> getFirst(): KottageListItem<T>?
    suspend fun <T: Any> getLast(): KottageListItem<T>?

    /**
     * @throws NoSuchElementException positionId is invalid
     */
    @Throws(
        NoSuchElementException::class,
        CancellationException::class
    )
    suspend fun <T: Any> get(positionId: String): KottageListItem<T>?
    /**
     * Get item with iteration.
     */
    @Throws(
        IndexOutOfBoundsException::class,
        CancellationException::class
    )
    suspend fun <T: Any> getByIndex(
        index: Long,
        direction: KottageListDirection = KottageListDirection.Forward
    ): KottageListItem<T>?

    suspend fun <T: Any> add(key: String, value: T)
    suspend fun <T: Any> addKey(key: String)
    suspend fun <T: Any> addAll(values: List<Pair<String, T>>)
    suspend fun <T: Any> addKeys(keys: List<String>)
    suspend fun <T: Any> addFirst(key: String, value: T)
    suspend fun <T: Any> addKeyFirst(key: String)
    suspend fun <T: Any> addAllFirst(values: List<Pair<String, T>>)
    suspend fun <T: Any> addKeysFirst(keys: List<String>)
    suspend fun <T: Any> update(positionId: String, key: String, value: T)
    suspend fun <T: Any> updateKey(positionId: String, key: String)
    suspend fun <T: Any> insertAfter(positionId: String, key: String, value: T)
    suspend fun <T: Any> insertKeyAfter(positionId: String, key: String)
    suspend fun <T: Any> insertAllAfter(positionId: String, values: Pair<String, T>)
    suspend fun <T: Any> insertKeysAfter(positionId: String, keys: List<String>)
    suspend fun <T: Any> insertBefore(positionId: String, value: T)
    suspend fun <T: Any> insertKeyBefore(positionId: String, key: String)
    suspend fun <T: Any> insertAllBefore(positionId: String, values: Pair<String, T>)
    suspend fun <T: Any> insertKeysBefore(positionId: String, keys: List<String>)
    suspend fun remove(positionId: String)
}
