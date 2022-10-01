package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageListDirection
import io.github.irgaly.kottage.KottageListItem
import io.github.irgaly.kottage.KottageListOptions
import io.github.irgaly.kottage.KottageListPage
import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.strategy.KottageStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KType

internal class KottageListImpl(
    override val name: String,
    override val storage: KottageStorage,
    private val strategy: KottageStrategy,
    private val encoder: Encoder,
    private val options: KottageListOptions,
    private val databaseManager: KottageDatabaseManager,
    private val calendar: KottageCalendar,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
): KottageList {
    private val itemType: String = storage.name
    private val listType: String = name
    private suspend fun operator() = databaseManager.operator.await()

    override suspend fun <T : Any> getPageFrom(
        positionId: String?,
        pageSize: Long?,
        type: KType,
        direction: KottageListDirection
    ): KottageListPage<T> {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        val items = databaseManager.transactionWithResult {
            val items = mutableListOf<KottageListItem<T>>()
            var initialPositionId = positionId
            if (initialPositionId == null) {
                initialPositionId = operator.getListStats(listType)?.let { stats ->
                    when (direction) {
                        KottageListDirection.Forward -> {
                            stats.firstItemPositionId
                        }

                        KottageListDirection.Backward -> {
                            stats.lastItemPositionId
                        }
                    }
                }
            }
            if (initialPositionId != null) {
                var nextPositionId: String? = initialPositionId
                while (
                    (pageSize?.let { items.size < it } != false)
                    && (nextPositionId != null)) {
                    operator.getListItem(listType, nextPositionId, direction)?.let { entry ->
                        nextPositionId = entry.nextId
                        val itemKey = checkNotNull(entry.itemKey)
                        val item = checkNotNull(
                            operator.getOrNull(
                                key = itemKey,
                                itemType = itemType,
                                null
                            )
                        )
                        strategy.onItemRead(
                            key = itemKey, itemType = itemType, now = now, operator = operator
                        )
                        items.add(
                            KottageListItem.from(
                                entry = entry,
                                itemKey = itemKey,
                                item = item,
                                type = type,
                                encoder = encoder
                            )
                        )
                    }
                }
            }
            if (direction == KottageListDirection.Backward) {
                // KottageListPage.items は常に Forward 順
                items.reverse()
            }
            items.toList()
        }
        return KottageListPage(
            items = items,
            previousPositionId = items.firstOrNull()?.previousPositionId,
            nextPositionId = items.lastOrNull()?.nextPositionId
        )
    }

    override suspend fun getSize(): Long {
        val operator = operator()
        return databaseManager.transactionWithResult {
            operator.getListCount(listType)
        }
    }

    override suspend fun <T : Any> getFirst(type: KType): KottageListItem<T>? {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        return databaseManager.transactionWithResult {
            operator.getListStats(listType)?.let { stats ->
                operator.getListItem(
                    listType, stats.firstItemPositionId,
                    KottageListDirection.Forward
                )?.let { entry ->
                    val itemKey = checkNotNull(entry.itemKey)
                    val item = checkNotNull(operator.getOrNull(key = itemKey, itemType = itemType, null))
                    strategy.onItemRead(
                        key = itemKey, itemType = itemType, now = now, operator = operator
                    )
                    KottageListItem.from(
                        entry = entry,
                        itemKey = itemKey,
                        item = item,
                        type = type,
                        encoder = encoder
                    )
                }
            }
        }
    }

    override suspend fun <T : Any> getLast(type: KType): KottageListItem<T>? {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        return databaseManager.transactionWithResult {
            operator.getListStats(listType)?.let { stats ->
                operator.getListItem(
                    listType, stats.lastItemPositionId,
                    KottageListDirection.Backward
                )?.let { entry ->
                    val itemKey = checkNotNull(entry.itemKey)
                    val item = checkNotNull(operator.getOrNull(key = itemKey, itemType = itemType, null))
                    strategy.onItemRead(
                        key = itemKey, itemType = itemType, now = now, operator = operator
                    )
                    KottageListItem.from(
                        entry = entry,
                        itemKey = itemKey,
                        item = item,
                        type = type,
                        encoder = encoder
                    )
                }
            }
        }
    }

    override suspend fun <T : Any> get(positionId: String, type: KType): KottageListItem<T>? {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        return databaseManager.transactionWithResult {
            operator.getListItem(
                listType, positionId,
                KottageListDirection.Forward
            )?.let { entry ->
                val itemKey = checkNotNull(entry.itemKey)
                val item = checkNotNull(operator.getOrNull(key = itemKey, itemType = itemType, null))
                strategy.onItemRead(
                    key = itemKey, itemType = itemType, now = now, operator = operator
                )
                KottageListItem.from(
                    entry = entry,
                    itemKey = itemKey,
                    item = item,
                    type = type,
                    encoder = encoder
                )
            }
        }
    }

    override suspend fun <T : Any> getByIndex(
        index: Long,
        type: KType,
        direction: KottageListDirection
    ): KottageListItem<T>? {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        return databaseManager.transactionWithResult {
            val initialPositionId = operator.getListStats(listType)?.let { stats ->
                when (direction) {
                    KottageListDirection.Forward -> {
                        stats.firstItemPositionId
                    }

                    KottageListDirection.Backward -> {
                        stats.lastItemPositionId
                    }
                }
            }
            var currentIndex = -1L
            var currentEntry: ItemListEntry? = null
            var nextIndex = 0L
            var nextPositionId = initialPositionId
            while (
                (nextIndex <= index)
                && (nextPositionId != null)
            ) {
                currentEntry = operator.getListItem(listType, nextPositionId, direction)
                currentIndex = nextIndex
                nextIndex++
                nextPositionId = currentEntry?.nextId
            }
            if (currentEntry != null && currentIndex == index) {
                // index のアイテムを見つけた
                val itemKey = checkNotNull(currentEntry.itemKey)
                val item =
                    checkNotNull(operator.getOrNull(key = itemKey, itemType = itemType, null))
                strategy.onItemRead(
                    key = itemKey, itemType = itemType, now = now, operator = operator
                )
                KottageListItem.from(
                    entry = currentEntry,
                    itemKey = itemKey,
                    item = item,
                    type = type,
                    encoder = encoder
                )
            } else null
        }
    }

    override suspend fun <T : Any> add(key: String, value: T, type: KType) {
        TODO("Not yet implemented")
    }

    override suspend fun addKey(key: String) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> addAll(values: List<Pair<String, T>>, type: KType) {
        TODO("Not yet implemented")
    }

    override suspend fun addKeys(keys: List<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> addFirst(key: String, value: T, type: KType) {
        TODO("Not yet implemented")
    }

    override suspend fun addKeyFirst(key: String) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> addAllFirst(values: List<Pair<String, T>>, type: KType) {
        TODO("Not yet implemented")
    }

    override suspend fun addKeysFirst(keys: List<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> update(positionId: String, key: String, value: T, type: KType) {
        TODO("Not yet implemented")
    }

    override suspend fun updateKey(positionId: String, key: String) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> insertAfter(
        positionId: String,
        key: String,
        value: T,
        type: KType
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun insertKeyAfter(positionId: String, key: String) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> insertAllAfter(
        positionId: String,
        values: Pair<String, T>,
        type: KType
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun insertKeysAfter(positionId: String, keys: List<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> insertBefore(positionId: String, value: T, type: KType) {
        TODO("Not yet implemented")
    }

    override suspend fun insertKeyBefore(positionId: String, key: String) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> insertAllBefore(
        positionId: String,
        values: Pair<String, T>,
        type: KType
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun insertKeysBefore(positionId: String, keys: List<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(positionId: String) {
        val operator = operator()
        return databaseManager.transaction {
            operator.removeListItem(positionId = positionId, listType = listType)
        }
    }
}
