package io.github.irgaly.kottage.internal.extension

import com.juul.indexeddb.Key
import com.juul.indexeddb.Queryable
import com.juul.indexeddb.WriteTransaction
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

/**
 * chunkSize ごとにアイテムを処理する
 * 重複可能な SortKey にも対応
 *
 * cursor を処理している間に対象が delete されると cursor がずれてしまうため
 * chunkSize ごとに cursor を閉じて処理する
 */
internal suspend fun <Item, SortKey> Queryable.iterateWithChunk(
    transaction: WriteTransaction,
    chunkSize: Long,
    primaryKey: (item: Item) -> String,
    sortKey: (item: Item) -> SortKey,
    initialRange: Key,
    resumeRange: (lastItem: Item) -> Key,
    limit: Long? = null,
    block: suspend (items: Item) -> Boolean
) {
    with(transaction) {
        var takeNext = true
        var consumed = 0L
        var consumedKeys = mapOf<SortKey, Set<String>>()
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
