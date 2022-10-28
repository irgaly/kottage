package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemEvent

internal class KottageSqliteItemEventRepository(
    private val database: KottageDatabase
) : KottageItemEventRepository {
    override fun create(transaction: Transaction, itemEvent: ItemEvent) {
        database.item_eventQueries
            .insert(itemEvent.toEntity())
    }

    override fun selectAfter(
        transaction: Transaction,
        createdAt: Long,
        itemType: String?,
        limit: Long?
    ): List<ItemEvent> {
        return if (itemType != null) {
            if (limit != null) {
                database.item_eventQueries
                    .selectItemTypeAfterCreatedAtLimit(
                        item_type = itemType,
                        created_at = createdAt,
                        limit = limit
                    ).executeAsList()
            } else {
                database.item_eventQueries
                    .selectItemTypeAfterCreatedAt(
                        item_type = itemType,
                        created_at = createdAt
                    ).executeAsList()
            }
        } else {
            if (limit != null) {
                database.item_eventQueries
                    .selectAfterCreatedAtLimit(
                        created_at = createdAt,
                        limit = limit
                    ).executeAsList()
            } else {
                database.item_eventQueries
                    .selectAfterCreatedAt(
                        created_at = createdAt
                    ).executeAsList()
            }
        }.map {
            it.toDomain()
        }
    }

    override fun getLatestCreatedAt(transaction: Transaction, itemType: String): Long? {
        return database.item_eventQueries
            .selectItemTypeLatestCreatedAt(itemType)
            .executeAsOneOrNull()
    }

    override fun getExpiredIds(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: (id: String, itemType: String) -> Unit
    ) {
        if (itemType != null) {
            database.item_eventQueries
                .selectExpiredIds(itemType, now)
                .execute().use { cursor ->
                    while (cursor.next()) {
                        val id = checkNotNull(cursor.getString(0))
                        receiver(id, itemType)
                    }
                }
        } else {
            database.item_eventQueries
                .selectAllTypeExpiredIds(now)
                .execute().use { cursor ->
                    while (cursor.next()) {
                        val id = checkNotNull(cursor.getString(0))
                        val type = checkNotNull(cursor.getString(1))
                        receiver(id, type)
                    }
                }
        }
    }

    override fun getCount(transaction: Transaction, itemType: String): Long {
        return database.item_eventQueries
            .countByType(itemType)
            .executeAsOne()
    }

    override fun delete(transaction: Transaction, id: String) {
        database.item_eventQueries
            .delete(id)
    }

    override fun deleteOlderEvents(transaction: Transaction, itemType: String, limit: Long) {
        database.item_eventQueries
            .selectOlderCreatedIds(itemType, limit)
            .execute().use { cursor ->
                while (cursor.next()) {
                    val id = checkNotNull(cursor.getString(0))
                    database.item_eventQueries
                        .delete(id)
                }
            }
    }

    override fun deleteBefore(transaction: Transaction, createdAt: Long) {
        database.item_eventQueries
            .deleteBefore(createdAt)
    }

    override fun deleteAll(transaction: Transaction, itemType: String) {
        database.item_eventQueries
            .deleteAllByType(itemType)
    }

    override fun deleteAllList(transaction: Transaction, listType: String) {
        database.item_eventQueries
            .deleteAllByListType(item_list_type = listType)
    }

    override fun getStatsCount(transaction: Transaction, itemType: String): Long {
        return database.item_statsQueries
            .select(itemType)
            .executeAsOneOrNull()?.event_count ?: 0
    }

    override fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .incrementEventCount(count, itemType)
    }

    override fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .decrementEventCount(count, itemType)
    }

    override fun updateStatsCount(transaction: Transaction, itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .updateEventCount(count, itemType)
    }
}
