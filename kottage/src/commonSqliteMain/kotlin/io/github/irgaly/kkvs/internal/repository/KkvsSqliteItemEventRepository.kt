package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.data.sqlite.KkvsDatabase
import io.github.irgaly.kkvs.internal.model.ItemEvent

internal class KkvsSqliteItemEventRepository(private val database: KkvsDatabase) :
    KkvsItemEventRepository {
    override fun create(itemEvent: ItemEvent) {
        database.item_eventQueries
            .insert(itemEvent.toEntity())
    }

    override fun selectAfter(itemType: String, createdAt: Long): List<ItemEvent> {
        return database.item_eventQueries
            .selectItemTypeAfter(
                item_type = itemType,
                created_at = createdAt
            )
            .executeAsList()
            .map { it.toDomain() }
    }

    override fun selectAfter(createdAt: Long): List<ItemEvent> {
        return database.item_eventQueries
            .selectAfter(created_at = createdAt)
            .executeAsList()
            .map { it.toDomain() }
    }

    override fun deleteBefore(createdAt: Long) {
        database.item_eventQueries
            .deleteBefore(createdAt)
    }
}
