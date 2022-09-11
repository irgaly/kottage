package io.github.irgaly.kottage.internal.repository

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

    override fun deleteBefore(createdAt: Long) {
        database.item_eventQueries
            .deleteBefore(createdAt)
    }
}
