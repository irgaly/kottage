package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemEvent

internal class KottageSqliteItemEventRepository(
    private val database: KottageDatabase
) : KottageItemEventRepository {
    override suspend fun create(transaction: Transaction, itemEvent: ItemEvent) {
        database.item_eventQueries
            .insert(itemEvent.toEntity())
    }

    override suspend fun selectAfter(
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

    override suspend fun getLatestCreatedAt(transaction: Transaction): Long? {
        return database.item_eventQueries
            .selectLatestCreatedAt()
            .executeAsOneOrNull()
    }

    override suspend fun getCount(transaction: Transaction, itemType: String): Long {
        return database.item_eventQueries
            .countByType(itemType)
            .executeAsOne()
    }

    override suspend fun delete(transaction: Transaction, id: String) {
        database.item_eventQueries
            .delete(id)
    }

    override suspend fun deleteExpiredEvents(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        onDelete: (suspend (id: String, itemType: String) -> Unit)?
    ): Long {
        var deleted = 0L
        if (itemType != null) {
            database.item_eventQueries
                .selectExpiredIds(itemType, now)
                .execute().use { cursor ->
                    while (cursor.next()) {
                        val id = checkNotNull(cursor.getString(0))
                        database.item_eventQueries
                            .delete(id)
                        onDelete?.invoke(id, itemType)
                        deleted++
                    }
                }
        } else {
            database.item_eventQueries
                .selectAllTypeExpiredIds(now)
                .execute().use { cursor ->
                    while (cursor.next()) {
                        val id = checkNotNull(cursor.getString(0))
                        val type = checkNotNull(cursor.getString(1))
                        database.item_eventQueries
                            .delete(id)
                        onDelete?.invoke(id, type)
                        deleted++
                    }
                }
        }
        return deleted
    }

    override suspend fun deleteOlderEvents(
        transaction: Transaction,
        itemType: String,
        limit: Long
    ) {
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

    override suspend fun deleteBefore(transaction: Transaction, createdAt: Long) {
        database.item_eventQueries
            .deleteBefore(createdAt)
    }

    override suspend fun deleteAll(transaction: Transaction, itemType: String) {
        database.item_eventQueries
            .deleteAllByType(itemType)
    }

    override suspend fun deleteAllList(transaction: Transaction, listType: String) {
        database.item_eventQueries
            .deleteAllByListType(item_list_type = listType)
    }

    override suspend fun getStatsCount(transaction: Transaction, itemType: String): Long {
        return database.item_statsQueries
            .select(itemType)
            .executeAsOneOrNull()?.event_count ?: 0L
    }

    override suspend fun incrementStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .incrementEventCount(count, itemType)
    }

    override suspend fun decrementStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .decrementEventCount(count, itemType)
    }

    override suspend fun updateStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .updateEventCount(count, itemType)
    }
}
