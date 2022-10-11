package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageListDirection
import io.github.irgaly.kottage.KottageListEntry
import io.github.irgaly.kottage.KottageListMetaData
import io.github.irgaly.kottage.KottageListOptions
import io.github.irgaly.kottage.KottageListPage
import io.github.irgaly.kottage.KottageListValue
import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.encoder.encodeItem
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.platform.Id
import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.strategy.KottageStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.reflect.KType

internal class KottageListImpl(
    override val name: String,
    override val storage: KottageStorage,
    private val encoder: Encoder,
    override val options: KottageListOptions,
    private val kottageOptions: KottageOptions,
    private val databaseManager: KottageDatabaseManager,
    private val calendar: KottageCalendar,
    private val onCompactionRequired: suspend () -> Unit,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
): KottageList {
    private val itemType: String = storage.name
    private val listType: String = name
    private val strategy: KottageStrategy = storage.options.strategy
    private suspend fun operator() = databaseManager.operator.await()

    @OptIn(DelicateCoroutinesApi::class)
    private val storageOperator = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        databaseManager.getStorageOperator(storage)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private val listOperator = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        databaseManager.getListOperator(this@KottageListImpl, storage)
    }

    override suspend fun getPageFrom(
        positionId: String?,
        pageSize: Long?,
        direction: KottageListDirection
    ): KottageListPage = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val items = transactionWithAutoCompaction { operator, now ->
            val items = mutableListOf<KottageListEntry>()
            var initialPositionId = positionId
            if (initialPositionId == null) {
                initialPositionId = operator.getListStats(listType)?.let { stats ->
                    when (direction) {
                        KottageListDirection.Forward -> stats.firstItemPositionId
                        KottageListDirection.Backward -> stats.lastItemPositionId
                    }
                }
            }
            if (initialPositionId != null) {
                listOperator.invalidateExpiredListEntries(now)
                var nextPositionId: String? = initialPositionId
                while (
                    (pageSize?.let { items.size < it } != false)
                    && (nextPositionId != null)
                ) {
                    listOperator.getAvailableListItem(
                        positionId = nextPositionId,
                        direction = direction
                    )?.let { entry ->
                        nextPositionId = entry.nextId
                        val itemKey = checkNotNull(entry.itemKey)
                        val item = checkNotNull(
                            storageOperator.getOrNull(key = itemKey, now = null)
                        )
                        strategy.onItemRead(
                            key = itemKey, itemType = itemType, now = now, operator = operator
                        )
                        items.add(
                            KottageListEntry.from(
                                entry = entry,
                                itemKey = itemKey,
                                item = item,
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
        KottageListPage(
            items = items,
            previousPositionId = items.firstOrNull()?.previousPositionId,
            nextPositionId = items.lastOrNull()?.nextPositionId
        )
    }

    override suspend fun getSize(): Long = withContext(dispatcher) {
        transactionWithAutoCompaction { operator, now ->
            operator.getListCount(listType = listType, now = now)
        }
    }

    override suspend fun isEmpty(): Boolean = withContext(dispatcher) {
        val count = transactionWithAutoCompaction { operator, now ->
            operator.getListCount(listType = listType, now = now)
        }
        (count <= 0)
    }

    override suspend fun isNotEmpty(): Boolean = withContext(dispatcher) {
        val count = transactionWithAutoCompaction { operator, now ->
            operator.getListCount(listType = listType, now = now)
        }
        (0 < count)
    }

    override suspend fun getFirst(): KottageListEntry? = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { operator, now ->
            operator.getListStats(listType)?.let { stats ->
                listOperator.invalidateExpiredListEntries(now)
                listOperator.getAvailableListItem(
                    positionId = stats.firstItemPositionId,
                    direction = KottageListDirection.Forward
                )?.let { entry ->
                    val itemKey = checkNotNull(entry.itemKey)
                    val item = checkNotNull(
                        storageOperator.getOrNull(key = itemKey, now = null)
                    )
                    strategy.onItemRead(
                        key = itemKey, itemType = itemType, now = now, operator = operator
                    )
                    KottageListEntry.from(
                        entry = entry,
                        itemKey = itemKey,
                        item = item,
                        encoder = encoder
                    )
                }
            }
        }
    }

    override suspend fun getLast(): KottageListEntry? = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { operator, now ->
            operator.getListStats(listType)?.let { stats ->
                listOperator.invalidateExpiredListEntries(now)
                listOperator.getAvailableListItem(
                    positionId = stats.lastItemPositionId,
                    direction = KottageListDirection.Backward
                )?.let { entry ->
                    val itemKey = checkNotNull(entry.itemKey)
                    val item = checkNotNull(
                        storageOperator.getOrNull(key = itemKey, now = null)
                    )
                    strategy.onItemRead(
                        key = itemKey, itemType = itemType, now = now, operator = operator
                    )
                    KottageListEntry.from(
                        entry = entry,
                        itemKey = itemKey,
                        item = item,
                        encoder = encoder
                    )
                }
            }
        }
    }

    override suspend fun get(positionId: String): KottageListEntry? = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { operator, now ->
            listOperator.invalidateExpiredListEntries(now)
            listOperator.getAvailableListItem(
                positionId = positionId,
                direction = KottageListDirection.Forward
            )?.let { entry ->
                val itemKey = checkNotNull(entry.itemKey)
                val item = checkNotNull(storageOperator.getOrNull(key = itemKey, now = null))
                strategy.onItemRead(
                    key = itemKey, itemType = itemType, now = now, operator = operator
                )
                KottageListEntry.from(
                    entry = entry,
                    itemKey = itemKey,
                    item = item,
                    encoder = encoder
                )
            }
        }
    }

    override suspend fun getByIndex(
        index: Long,
        direction: KottageListDirection
    ): KottageListEntry? = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { operator, now ->
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
            listOperator.invalidateExpiredListEntries(now)
            while (
                (nextIndex <= index)
                && (nextPositionId != null)
            ) {
                currentEntry = listOperator.getAvailableListItem(
                    positionId = nextPositionId,
                    direction = direction
                )
                currentIndex = nextIndex
                nextIndex++
                nextPositionId = currentEntry?.nextId
            }
            if (currentEntry != null && currentIndex == index) {
                // index のアイテムを見つけた
                val itemKey = checkNotNull(currentEntry.itemKey)
                val item =
                    checkNotNull(storageOperator.getOrNull(key = itemKey, now = null))
                strategy.onItemRead(
                    key = itemKey, itemType = itemType, now = now, operator = operator
                )
                KottageListEntry.from(
                    entry = currentEntry,
                    itemKey = itemKey,
                    item = item,
                    encoder = encoder
                )
            } else null
        }
    }

    override suspend fun <T : Any> add(
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction(now) { _, _ ->
            val lastPositionId = listOperator.getLastItemPositionId()
            storageOperator.upsertItem(item, now)
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = lastPositionId,
                nextId = null,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun addKey(
        key: String, metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction(now) { _, _ ->
            val item = storageOperator.getOrNull(key = key, now = null)
                ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
            val lastPositionId = listOperator.getLastItemPositionId()
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = lastPositionId,
                nextId = null,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun addAll(
        values: List<KottageListValue<*>>
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, it.type, now)
            Triple(id, item, it.metaData)
        }
        transactionWithAutoCompaction(now) { _, _ ->
            val lastPositionId = listOperator.getLastItemPositionId()
            val entries = items.mapIndexed { index, (id, item, metaData) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first ?: lastPositionId,
                    nextId = items.getOrNull(index + 1)?.first,
                    now = now,
                    metaData = metaData
                )
            }
            items.forEach { (_, item, _) ->
                storageOperator.upsertItem(item, now)
            }
            listOperator.addListEntries(entries, now)
        }
        if (values.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun addKeys(
        keys: List<String>, metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { _, now ->
            val lastPositionId = listOperator.getLastItemPositionId()
            val items = keys.map { key ->
                val id = Id.generateUuidV4Short()
                val item = storageOperator.getOrNull(key = key, now = null)
                    ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
                Pair(id, item)
            }
            val entries = items.mapIndexed { index, (id, item) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first ?: lastPositionId,
                    nextId = items.getOrNull(index + 1)?.first,
                    now = now,
                    metaData = metaData
                )
            }
            listOperator.addListEntries(entries, now)
        }
        if (keys.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun <T : Any> addFirst(
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction(now) { _, _ ->
            val firstPositionId = listOperator.getFirstItemPositionId()
            storageOperator.upsertItem(item, now)
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = null,
                nextId = firstPositionId,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun addKeyFirst(
        key: String, metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction { _, now ->
            val item = storageOperator.getOrNull(key = key, now = null)
                ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
            val firstPositionId = listOperator.getFirstItemPositionId()
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = null,
                nextId = firstPositionId,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun addAllFirst(
        values: List<KottageListValue<*>>
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, it.type, now)
            Triple(id, item, it.metaData)
        }
        transactionWithAutoCompaction(now) { _, _ ->
            val firstPositionId = listOperator.getFirstItemPositionId()
            val entries = items.mapIndexed { index, (id, item, metaData) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first,
                    nextId = items.getOrNull(index + 1)?.first ?: firstPositionId,
                    now = now,
                    metaData = metaData
                )
            }
            items.forEach { (_, item, _) ->
                storageOperator.upsertItem(item, now)
            }
            listOperator.addListEntries(entries, now)
        }
        if (values.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun addKeysFirst(
        keys: List<String>,
        metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { _, now ->
            val firstPositionId = listOperator.getFirstItemPositionId()
            val items = keys.map { key ->
                val id = Id.generateUuidV4Short()
                val item = storageOperator.getOrNull(key = key, now = null)
                    ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
                Pair(id, item)
            }
            val entries = items.mapIndexed { index, (id, item) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first,
                    nextId = items.getOrNull(index + 1)?.first ?: firstPositionId,
                    now = now,
                    metaData = metaData
                )
            }
            listOperator.addListEntries(entries, now)
        }
        if (keys.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun <T : Any> update(
        positionId: String,
        key: String,
        value: T,
        type: KType
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val entry = transactionWithAutoCompaction(now) { _, _ ->
            val entry = listOperator.getListItem(positionId = positionId)
                ?: throw NoSuchElementException("positionId = $positionId")
            storageOperator.upsertItem(item, now)
            listOperator.updateItemKey(entry.id, item, now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun updateKey(
        positionId: String,
        key: String
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val entry = transactionWithAutoCompaction { _, now ->
            val item = storageOperator.getOrNull(key = key, now = null)
                ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
            val entry = listOperator.getListItem(positionId = positionId)
                ?: throw NoSuchElementException("positionId = $positionId")
            listOperator.updateItemKey(entry.id, item, now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun <T : Any> insertAfter(
        positionId: String,
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction(now) { _, _ ->
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            storageOperator.upsertItem(item, now)
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = anchorEntry.id,
                nextId = anchorEntry.nextId,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun insertKeyAfter(
        positionId: String,
        key: String,
        metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction { _, now ->
            val item = storageOperator.getOrNull(key = key, now = null)
                ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = anchorEntry.id,
                nextId = anchorEntry.nextId,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun insertAllAfter(
        positionId: String,
        values: List<KottageListValue<*>>
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, it.type, now)
            Triple(id, item, it.metaData)
        }
        transactionWithAutoCompaction(now) { _, _ ->
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            val entries = items.mapIndexed { index, (id, item, metaData) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first ?: anchorEntry.id,
                    nextId = items.getOrNull(index + 1)?.first ?: anchorEntry.nextId,
                    now = now,
                    metaData = metaData
                )
            }
            items.forEach { (_, item, _) ->
                storageOperator.upsertItem(item, now)
            }
            listOperator.addListEntries(entries, now)
        }
        if (values.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun insertKeysAfter(
        positionId: String,
        keys: List<String>,
        metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { _, now ->
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            val items = keys.map { key ->
                val id = Id.generateUuidV4Short()
                val item = storageOperator.getOrNull(key = key, now = null)
                    ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
                Pair(id, item)
            }
            val entries = items.mapIndexed { index, (id, item) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first ?: anchorEntry.id,
                    nextId = items.getOrNull(index + 1)?.first ?: anchorEntry.nextId,
                    now = now,
                    metaData = metaData
                )
            }
            listOperator.addListEntries(entries, now)
        }
        if (keys.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun <T : Any> insertBefore(
        positionId: String,
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction(now) { _, _ ->
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            storageOperator.upsertItem(item, now)
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = anchorEntry.previousId,
                nextId = anchorEntry.id,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun insertKeyBefore(
        positionId: String,
        key: String,
        metaData: KottageListMetaData?
    ): KottageListEntry = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val newPositionId = Id.generateUuidV4Short()
        val entry = transactionWithAutoCompaction { _, now ->
            val item = storageOperator.getOrNull(key = key, now = null)
                ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            val entry = createItemListEntry(
                id = newPositionId,
                itemKey = item.key,
                previousId = anchorEntry.previousId,
                nextId = anchorEntry.id,
                now = now,
                metaData = metaData
            )
            listOperator.addListEntries(listOf(entry), now)
            KottageListEntry.from(
                entry = entry,
                itemKey = item.key,
                item = item,
                encoder = encoder
            )
        }
        databaseManager.onEventCreated()
        entry
    }

    override suspend fun insertAllBefore(
        positionId: String,
        values: List<KottageListValue<*>>
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, it.type, now)
            Triple(id, item, it.metaData)
        }
        transactionWithAutoCompaction(now) { _, _ ->
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            val entries = items.mapIndexed { index, (id, item, metaData) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first ?: anchorEntry.previousId,
                    nextId = items.getOrNull(index + 1)?.first ?: anchorEntry.id,
                    now = now,
                    metaData = metaData
                )
            }
            items.forEach { (_, item, _) ->
                storageOperator.upsertItem(item, now)
            }
            listOperator.addListEntries(entries, now)
        }
        if (values.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun insertKeysBefore(
        positionId: String,
        keys: List<String>,
        metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        transactionWithAutoCompaction { _, now ->
            val anchorEntry = listOperator.getListItem(positionId)
                ?: throw NoSuchElementException("list = $listType, positionId = $positionId")
            val items = keys.map { key ->
                val id = Id.generateUuidV4Short()
                val item = storageOperator.getOrNull(key = key, now = null)
                    ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
                Pair(id, item)
            }
            val entries = items.mapIndexed { index, (id, item) ->
                createItemListEntry(
                    id = id,
                    itemKey = item.key,
                    previousId = items.getOrNull(index - 1)?.first ?: anchorEntry.previousId,
                    nextId = items.getOrNull(index + 1)?.first ?: anchorEntry.id,
                    now = now,
                    metaData = metaData
                )
            }
            listOperator.addListEntries(entries, now)
        }
        if (keys.isNotEmpty()) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun remove(positionId: String) = withContext(dispatcher) {
        val listOperator = listOperator.await()
        val removed = transactionWithAutoCompaction { _, now ->
            listOperator.removeListItem(positionId = positionId, now = now)
        }
        if (removed) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun compact() = withContext(dispatcher) {
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
            listOperator.evictExpiredEntries(now)
        }
        storage.compact()
    }

    override suspend fun clear() = withContext(dispatcher) {
        val listOperator = listOperator.await()
        databaseManager.transaction {
            listOperator.clear()
        }
    }

    override suspend fun getDebugStatus(): String = withContext(dispatcher) {
        val listOperator = listOperator.await()
        databaseManager.transactionWithResult {
            listOperator.getDebugStatus()
        }
    }

    override suspend fun getDebugListRawData(): String = withContext(dispatcher) {
        val listOperator = listOperator.await()
        databaseManager.transactionWithResult {
            listOperator.getDebugListRawData()
        }
    }

    private fun createItemListEntry(
        id: String,
        itemKey: String,
        previousId: String?,
        nextId: String?,
        now: Long,
        metaData: KottageListMetaData?
    ): ItemListEntry {
        return ItemListEntry(
            id = id,
            type = listType,
            itemType = itemType,
            itemKey = itemKey,
            previousId = previousId,
            nextId = nextId,
            expireAt = options.itemExpireTime?.let { now + it.inWholeMilliseconds },
            userInfo = metaData?.info,
            userPreviousKey = metaData?.previousKey,
            userCurrentKey = metaData?.currentKey,
            userNextKey = metaData?.nextKey
        )
    }

    private suspend fun <R> transactionWithAutoCompaction(
        now: Long? = null,
        bodyWithReturn: (operator: KottageOperator, now: Long) -> R
    ): R {
        val operator = operator()
        val receivedNow = now ?: calendar.nowUnixTimeMillis()
        var compactionRequired = false
        val result = databaseManager.transactionWithResult {
            compactionRequired =
                operator.getAutoCompactionNeeded(receivedNow, kottageOptions.autoCompactionDuration)
            bodyWithReturn(operator, receivedNow)
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        return result
    }
}
