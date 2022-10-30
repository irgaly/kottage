package io.github.irgaly.kottage.internal.extension

import com.juul.indexeddb.Key
import com.juul.indexeddb.ObjectStore
import com.juul.indexeddb.Queryable
import com.juul.indexeddb.WriteTransaction
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

/**
 * chunkSize ごとにアイテムを処理する
 * 重複可能な SortKey にも対応
 */
internal suspend fun <Item, PrimaryKey, SortKey> Queryable.iterateWithChunk(
    transaction: WriteTransaction,
    chunkSize: Long,
    primaryKey: (item: Item) -> PrimaryKey,
    sortKey: (item: Item) -> SortKey,
    initialRange: Key,
    resumeRange: (lastItem: Item) -> Key,
    limit: Long? = null,
    block: suspend (items: Item) -> Boolean
) {
    with(transaction) {
        var takeNext = true
        var consumed = 0L
        var consumedKeys = mapOf<SortKey, Set<PrimaryKey>>()
        var nextRange = initialRange
        while (takeNext) {
            val results = openCursor(nextRange).map { cursor ->
                cursor.value.unsafeCast<Item>()
            }.filter {
                // すでに処理した項目はスキップする
                consumedKeys[sortKey(it)]?.let { consumed ->
                    (primaryKey(it) !in consumed)
                } ?: true
            }.take(
                limit?.let { (it - consumed).coerceAtMost(chunkSize) } ?: chunkSize
            ).toList()
            var continueNext = true
            results.forEach { item ->
                if (continueNext) {
                    val canNext = block(item)
                    consumed++
                    continueNext = (canNext && (limit?.let { consumed < it } ?: true))
                }
            }
            val hasNext = (chunkSize <= results.size)
            takeNext = (continueNext && hasNext)
            if (takeNext) {
                results.lastOrNull()?.let { lastItem ->
                    val nextSortKey = sortKey(lastItem)
                    nextRange = resumeRange(lastItem)
                    val keys = results.filter {
                        (sortKey(it) == nextSortKey)
                    }.map { primaryKey(it) }.toSet()
                        .union(consumedKeys[nextSortKey] ?: emptySet())
                    // nextSortKey の項目だけを記憶する
                    consumedKeys = mapOf(nextSortKey to keys)
                }
            }
        }
    }
}

/**
 * chunkSize ごとにデータを取得して削除する
 */
internal suspend fun <Item, PrimaryKey> Queryable.deleteWithChunk(
    transaction: WriteTransaction,
    store: ObjectStore,
    query: Key,
    chunkSize: Long,
    primaryKey: (item: Item) -> PrimaryKey,
    limit: Long? = null,
    onDelete: (suspend (items: Item) -> Unit)? = null
): Long {
    return with(transaction) {
        var takeNext = true
        var deleted = 0L
        while (takeNext) {
            val results = openCursor(query)
                .take(
                    limit?.let { (it - deleted).coerceAtMost(chunkSize) } ?: chunkSize
                ).map {
                    it.value.unsafeCast<Item>()
                }.toList()
            results.forEach {
                store.delete(Key(primaryKey(it)))
                deleted++
                onDelete?.invoke(it)
            }
            val hasNext = (chunkSize <= results.size)
            takeNext = (hasNext && (limit?.let { deleted < it } ?: true))
        }
        deleted
    }
}
