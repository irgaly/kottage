package io.github.irgaly.kkvs.internal.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.irgaly.kkvs.data.sqlite.Item_event
import io.github.irgaly.kkvs.data.sqlite.KkvsDatabase
import io.github.irgaly.kkvs.internal.model.ItemEvent

internal class KkvsSqliteItemEventRepository(
    private val database: KkvsDatabase
) : KkvsItemEventRepository {
    override suspend fun create(itemEvent: ItemEvent) = withContext(Dispatchers.Default) {
        database.item_eventQueries
            .insert(
                Item_event(
                    created_at = itemEvent.createdAt,
                    item_type = itemEvent.itemType,
                    item_key = itemEvent.itemKey,
                    event_type = itemEvent.eventType.toEntity()
                )
            )
    }

    override suspend fun selectAfter(itemType: String, createdAt: Long): List<ItemEvent> =
        withContext(Dispatchers.Default) {
            database.item_eventQueries
                .selectItemTypeAfter(
                    item_type = itemType,
                    created_at = createdAt
                )
                .executeAsList()
                .map { it.toDomain() }
        }

    override suspend fun selectAfter(createdAt: Long) = withContext(Dispatchers.Default) {
        database.item_eventQueries
            .selectAfter(created_at = createdAt)
            .executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun deleteBefore(createdAt: Long) = withContext(Dispatchers.Default) {
        database.item_eventQueries
            .deleteBefore(createdAt)
    }
}
