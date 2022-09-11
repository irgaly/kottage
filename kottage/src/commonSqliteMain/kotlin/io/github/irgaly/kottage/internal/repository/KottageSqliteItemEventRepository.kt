package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.internal.model.ItemEvent

internal class KottageSqliteItemEventRepository(
    private val database: KottageDatabase
) : KottageItemEventRepository {
    override fun create(itemEvent: ItemEvent) {
        database.item_eventQueries
            .insert(itemEvent.toEntity())
    }

    override fun selectAfter(
        itemType: String,
        createdAt: Long,
        limit: Long?
    ): List<ItemEvent> {
        return if (limit != null) {
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
        }.map {
            it.toDomain()
        }
    }

    override fun getLatestCreatedAt(itemType: String): Long? {
        return database.item_eventQueries
            .selectItemTypeLatestCreatedAt(itemType)
            .executeAsOneOrNull()
    }

    override fun getCount(itemType: String): Long {
        return database.item_eventQueries
            .countByType(itemType)
            .executeAsOne()
    }

    override fun deleteOlderEvents(itemType: String, limit: Long) {
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

    override fun deleteBefore(createdAt: Long) {
        database.item_eventQueries
            .deleteBefore(createdAt)
    }

    override fun deleteAll(itemType: String) {
        database.item_eventQueries
            .deleteAllByType(itemType)
    }

    override fun getStatsCount(itemType: String): Long {
        return database.item_statsQueries
            .select(itemType)
            .executeAsOneOrNull()?.event_count ?: 0
    }

    override fun incrementStatsCount(itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .incrementEventCount(count, itemType)
    }

    override fun decrementStatsCount(itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .decrementEventCount(count, itemType)
    }

    override fun updateStatsCount(itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .updateEventCount(count, itemType)
    }
}
