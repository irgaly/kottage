package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.data.sqlite.KkvsDatabase
import io.github.irgaly.kkvs.internal.model.ItemEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class KkvsSqliteItemEventRepository(
    private val database: KkvsDatabase
) : KkvsItemEventRepository {
    override suspend fun create(itemEvent: ItemEvent) = withContext(Dispatchers.Default) {
        database.item_eventQueries
            .insert(itemEvent.toEntity())
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
