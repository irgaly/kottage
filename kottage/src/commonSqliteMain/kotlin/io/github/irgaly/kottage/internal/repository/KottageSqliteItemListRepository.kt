package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.Item_list_stats
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal class KottageSqliteItemListRepository(
    private val database: KottageDatabase
) : KottageItemListRepository {
    override fun upsert(entry: ItemListEntry) {
        database.item_listQueries
            .replace(entry.toEntity())
    }

    override fun updatePreviousId(id: String, previousId: String?) {
        database.item_listQueries
            .updatePreviousId(
                previous_id = previousId,
                id = id
            )
    }

    override fun updateNextId(id: String, nextId: String?) {
        database.item_listQueries
            .updateNextId(
                next_id = nextId,
                id = id
            )
    }

    override fun updateItemKey(id: String, itemType: String, itemKey: String?, expireAt: Long?) {
        database.item_listQueries
            .updateItemKey(
                item_key = itemKey,
                item_type = itemType,
                expire_at = expireAt,
                id = id
            )
    }

    override fun updateExpireAt(id: String, expireAt: Long?) {
        database.item_listQueries
            .updateExpireAt(
                expire_at = expireAt,
                id = id
            )
    }

    override fun removeItemKey(id: String) {
        database.item_listQueries
            .removeItemKey(id = id)
    }

    override fun removeUserData(id: String) {
        database.item_listQueries
            .removeUserData(id = id)
    }

    override fun get(id: String): ItemListEntry? {
        return database.item_listQueries
            .select(id = id)
            .executeAsOneOrNull()?.toDomain()
    }

    override fun getIds(itemType: String, itemKey: String): List<String> {
        return database.item_listQueries
            .selectIdFromItem(
                item_type = itemType,
                item_key = itemKey
            ).executeAsList()
    }

    override fun getInvalidatedItemIds(
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

    override fun getCount(type: String): Long {
        return database.item_listQueries
            .countByType(type = type)
            .executeAsOne()
    }

    override fun getInvalidatedItemCount(type: String): Long {
        return database.item_listQueries
            .countInvalidatedItem(type = type)
            .executeAsOne()
    }

    override fun getAllTypes(receiver: (type: String) -> Unit) {
        database.item_list_statsQueries
            .selectAllItemListType()
            .execute().use { cursor ->
                while (cursor.next()) {
                    val itemListType = checkNotNull(cursor.getString(0))
                    receiver(itemListType)
                }
            }
    }

    override fun delete(id: String) {
        database.item_listQueries
            .delete(id = id)
    }

    override fun deleteAll(type: String) {
        database.item_listQueries
            .deleteAllByType(type = type)
    }

    override fun createStats(
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

    override fun getStats(type: String): ItemListStats? {
        return database.item_list_statsQueries
            .select(item_list_type = type)
            .executeAsOneOrNull()?.toDomain()
    }

    override fun getStatsCount(type: String): Long {
        return database.item_list_statsQueries
            .select(item_list_type = type)
            .executeAsOneOrNull()?.count ?: 0
    }

    override fun incrementStatsCount(type: String, count: Long) {
        database.item_list_statsQueries
            .incrementCount(
                count = count,
                item_list_type = type
            )
    }

    override fun decrementStatsCount(type: String, count: Long) {
        database.item_list_statsQueries
            .decrementCount(
                count = count,
                item_list_type = type
            )
    }

    override fun updateStatsCount(type: String, count: Long) {
        database.item_list_statsQueries
            .updateCount(
                count = count,
                item_list_type = type
            )
    }

    override fun updateStatsFirstItem(type: String, id: String) {
        database.item_list_statsQueries
            .updateFirstItemListId(
                first_item_list_id = id,
                item_list_type = type
            )
    }

    override fun updateStatsLastItem(type: String, id: String) {
        database.item_list_statsQueries
            .updateLastItemListId(
                last_item_list_id = id,
                item_list_type = type
            )
    }

    override fun deleteStats(type: String) {
        database.item_list_statsQueries
            .delete(item_list_type = type)
    }
}
