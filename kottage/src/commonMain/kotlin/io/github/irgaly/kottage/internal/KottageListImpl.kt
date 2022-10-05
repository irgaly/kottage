package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageListDirection
import io.github.irgaly.kottage.KottageListEntry
import io.github.irgaly.kottage.KottageListItem
import io.github.irgaly.kottage.KottageListMetaData
import io.github.irgaly.kottage.KottageListOptions
import io.github.irgaly.kottage.KottageListPage
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
    private val strategy: KottageStrategy,
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
    private suspend fun operator() = databaseManager.operator.await()

    @OptIn(DelicateCoroutinesApi::class)
    private val storageOperator = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        databaseManager.getStorageOperator(storage)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private val listOperator = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        databaseManager.getListOperator(this@KottageListImpl, storage)
    }

    override suspend fun <T : Any> getPageFrom(
        positionId: String?,
        pageSize: Long?,
        type: KType,
        direction: KottageListDirection
    ): KottageListPage<T> = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
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
                    operator.getAvailableListItem(listType, nextPositionId, direction)
                        ?.let { entry ->
                            nextPositionId = entry.nextId
                            val itemKey = checkNotNull(entry.itemKey)
                            val item = checkNotNull(
                                storageOperator.getOrNull(key = itemKey, now = null)
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
        KottageListPage(
            items = items,
            previousPositionId = items.firstOrNull()?.previousPositionId,
            nextPositionId = items.lastOrNull()?.nextPositionId
        )
    }

    override suspend fun getSize(): Long = withContext(dispatcher) {
        val operator = operator()
        databaseManager.transactionWithResult {
            operator.getListCount(listType)
        }
    }

    override suspend fun <T : Any> getFirst(
        type: KType
    ): KottageListItem<T>? = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transactionWithResult {
            operator.getListStats(listType)?.let { stats ->
                operator.getAvailableListItem(
                    listType, stats.firstItemPositionId,
                    KottageListDirection.Forward
                )?.let { entry ->
                    val itemKey = checkNotNull(entry.itemKey)
                    val item = checkNotNull(
                        storageOperator.getOrNull(key = itemKey, now = null)
                    )
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

    override suspend fun <T : Any> getLast(
        type: KType
    ): KottageListItem<T>? = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transactionWithResult {
            operator.getListStats(listType)?.let { stats ->
                operator.getAvailableListItem(
                    listType, stats.lastItemPositionId,
                    KottageListDirection.Backward
                )?.let { entry ->
                    val itemKey = checkNotNull(entry.itemKey)
                    val item = checkNotNull(
                        storageOperator.getOrNull(key = itemKey, now = null)
                    )
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

    override suspend fun <T : Any> get(
        positionId: String, type: KType
    ): KottageListItem<T>? = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transactionWithResult {
            operator.getAvailableListItem(
                listType, positionId,
                KottageListDirection.Forward
            )?.let { entry ->
                val itemKey = checkNotNull(entry.itemKey)
                val item = checkNotNull(storageOperator.getOrNull(key = itemKey, now = null))
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
    ): KottageListItem<T>? = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transactionWithResult {
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
                currentEntry = operator.getAvailableListItem(listType, nextPositionId, direction)
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

    override suspend fun <T : Any> add(
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        databaseManager.onEventCreated()
    }

    override suspend fun addKey(
        key: String, metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val newPositionId = Id.generateUuidV4Short()
        databaseManager.transaction {
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
        }
        databaseManager.onEventCreated()
    }

    override suspend fun <T : Any> addAll(
        values: List<KottageListEntry<T>>, type: KType
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, type, now)
            Triple(id, item, it.metaData)
        }
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
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
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
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
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        databaseManager.onEventCreated()
    }

    override suspend fun addKeyFirst(
        key: String, metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val newPositionId = Id.generateUuidV4Short()
        databaseManager.transaction {
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
        }
        databaseManager.onEventCreated()
    }

    override suspend fun <T : Any> addAllFirst(
        values: List<KottageListEntry<T>>,
        type: KType
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, type, now)
            Triple(id, item, it.metaData)
        }
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
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
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
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
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        var compactionRequired = false
        databaseManager.transaction {
            val entry = listOperator.getListItem(positionId = positionId)
                ?: throw NoSuchElementException("positionId = $positionId")
            storageOperator.upsertItem(item, now)
            listOperator.updateItemKey(entry.id, item, now)
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        databaseManager.onEventCreated()
    }

    override suspend fun updateKey(
        positionId: String,
        key: String
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
            val item = storageOperator.getOrNull(key = key, now = null)
                ?: throw NoSuchElementException("storage = ${storage.name}, key = $key")
            val entry = listOperator.getListItem(positionId = positionId)
                ?: throw NoSuchElementException("positionId = $positionId")
            listOperator.updateItemKey(entry.id, item, now)
        }
        databaseManager.onEventCreated()
    }

    override suspend fun <T : Any> insertAfter(
        positionId: String,
        key: String,
        value: T,
        type: KType,
        metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        databaseManager.onEventCreated()
    }

    override suspend fun insertKeyAfter(
        positionId: String,
        key: String,
        metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val newPositionId = Id.generateUuidV4Short()
        databaseManager.transaction {
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
        }
        databaseManager.onEventCreated()
    }

    override suspend fun <T : Any> insertAllAfter(
        positionId: String,
        values: List<KottageListEntry<T>>,
        type: KType
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, type, now)
            Triple(id, item, it.metaData)
        }
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
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
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
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
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val item = encoder.encodeItem(storage, key, value, type, now)
        val newPositionId = Id.generateUuidV4Short()
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        databaseManager.onEventCreated()
    }

    override suspend fun insertKeyBefore(
        positionId: String,
        key: String,
        metaData: KottageListMetaData?
    ) = withContext(dispatcher) {
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val newPositionId = Id.generateUuidV4Short()
        databaseManager.transaction {
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
        }
        databaseManager.onEventCreated()
    }

    override suspend fun <T : Any> insertAllBefore(
        positionId: String,
        values: List<KottageListEntry<T>>,
        type: KType
    ) = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val listOperator = listOperator.await()
        val now = calendar.nowUnixTimeMillis()
        val items = values.map {
            val id = Id.generateUuidV4Short()
            val item = encoder.encodeItem(storage, it.key, it.value, type, now)
            Triple(id, item, it.metaData)
        }
        var compactionRequired = false
        databaseManager.transaction {
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
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
        }
        if (compactionRequired) {
            onCompactionRequired()
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
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
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
        databaseManager.transaction {
            listOperator.removeListItem(positionId = positionId)
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
            userPreviousKey = metaData?.previousKey,
            userCurrentKey = metaData?.currentKey,
            userNextKey = metaData?.nextKey
        )
    }
}
