package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.Item_list_stats
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal class KottageSqliteItemListRepository(
    private val database: KottageDatabase
) : KottageItemListRepository {
    override suspend fun upsert(transaction: Transaction, entry: ItemListEntry) {
        database.item_listQueries
            .replace(entry.toEntity())
    }

    override suspend fun updatePreviousId(
        transaction: Transaction,
        id: String,
        previousId: String?
    ) {
        database.item_listQueries
            .updatePreviousId(
                previous_id = previousId,
                id = id
            )
    }

    override suspend fun updateNextId(transaction: Transaction, id: String, nextId: String?) {
        database.item_listQueries
            .updateNextId(
                next_id = nextId,
                id = id
            )
    }

    override suspend fun updateItemKey(
        transaction: Transaction,
        id: String,
        itemType: String,
        itemKey: String?,
        expireAt: Long?
    ) {
        database.item_listQueries
            .updateItemKey(
                item_key = itemKey,
                item_type = itemType,
                expire_at = expireAt,
                id = id
            )
    }

    override suspend fun updateExpireAt(
        transaction: Transaction,
        id: String,
        expireAt: Long?
    ) {
        database.item_listQueries
            .updateExpireAt(
                expire_at = expireAt,
                id = id
            )
    }

    override suspend fun removeItemKey(transaction: Transaction, id: String) {
        database.item_listQueries
            .removeItemKey(id = id)
    }

    override suspend fun removeUserData(transaction: Transaction, id: String) {
        database.item_listQueries
            .removeUserData(id = id)
    }

    override suspend fun get(transaction: Transaction, id: String): ItemListEntry? {
        return database.item_listQueries
            .select(id = id)
            .executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getIds(
        transaction: Transaction,
        itemType: String,
        itemKey: String
    ): List<String> {
        return database.item_listQueries
            .selectIdFromItem(
                item_type = itemType,
                item_key = itemKey
            ).executeAsList()
    }

    override suspend fun getInvalidatedItemIds(
        transaction: Transaction,
        type: String,
        beforeExpireAt: Long?,
        limit: Long
    ): List<String> {
        return if (beforeExpireAt != null) {
            database.item_listQueries
                .selectInvalidatedItemBeforeExpireAt(
                    type = type,
                    expire_at = beforeExpireAt,
                    limit = limit
                ).executeAsList()
        } else {
            database.item_listQueries
                .selectInvalidatedItem(
                    type = type,
                    limit = limit
                ).executeAsList()
        }
    }

    override suspend fun getCount(transaction: Transaction, type: String): Long {
        return database.item_listQueries
            .countByType(type = type)
            .executeAsOne()
    }

    override suspend fun getInvalidatedItemCount(
        transaction: Transaction,
        type: String
    ): Long {
        return database.item_listQueries
            .countInvalidatedItem(type = type)
            .executeAsOne()
    }

    override suspend fun getAllTypes(
        transaction: Transaction,
        receiver: suspend (type: String) -> Unit
    ) {
        database.item_list_statsQueries
            .selectAllItemListType()
            .execute().use { cursor ->
                while (cursor.next()) {
                    val itemListType = checkNotNull(cursor.getString(0))
                    receiver(itemListType)
                }
            }
    }

    override suspend fun delete(transaction: Transaction, id: String) {
        database.item_listQueries
            .delete(id = id)
    }

    override suspend fun deleteAll(transaction: Transaction, type: String) {
        database.item_listQueries
            .deleteAllByType(type = type)
    }

    override suspend fun createStats(
        transaction: Transaction,
        type: String,
        count: Long,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    ) {
        database.item_list_statsQueries
            .insert(
                Item_list_stats(
                    item_list_type = type,
                    count = count,
                    first_item_list_id = firstItemListEntryId,
                    last_item_list_id = lastItemListEntryId
                )
            )
    }

    override suspend fun getStats(transaction: Transaction, type: String): ItemListStats? {
        return database.item_list_statsQueries
            .select(item_list_type = type)
            .executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getStatsCount(transaction: Transaction, type: String): Long {
        return database.item_list_statsQueries
            .select(item_list_type = type)
            .executeAsOneOrNull()?.count ?: 0L
    }

    override suspend fun incrementStatsCount(
        transaction: Transaction,
        type: String,
        count: Long
    ) {
        database.item_list_statsQueries
            .incrementCount(
                count = count,
                item_list_type = type
            )
    }

    override suspend fun decrementStatsCount(
        transaction: Transaction,
        type: String,
        count: Long
    ) {
        database.item_list_statsQueries
            .decrementCount(
                count = count,
                item_list_type = type
            )
    }

    override suspend fun updateStatsCount(
        transaction: Transaction,
        type: String,
        count: Long
    ) {
        database.item_list_statsQueries
            .updateCount(
                count = count,
                item_list_type = type
            )
    }

    override suspend fun updateStatsFirstItem(
        transaction: Transaction,
        type: String,
        id: String
    ) {
        database.item_list_statsQueries
            .updateFirstItemListId(
                first_item_list_id = id,
                item_list_type = type
            )
    }

    override suspend fun updateStatsLastItem(
        transaction: Transaction,
        type: String,
        id: String
    ) {
        database.item_list_statsQueries
            .updateLastItemListId(
                last_item_list_id = id,
                item_list_type = type
            )
    }

    override suspend fun deleteStats(transaction: Transaction, type: String) {
        database.item_list_statsQueries
            .delete(item_list_type = type)
    }
}
