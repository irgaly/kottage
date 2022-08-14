package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.data.sqlite.Item_event
import net.irgaly.kkvs.data.sqlite.KkvsDatabase
import net.irgaly.kkvs.internal.model.ItemEvent

class KkvsSqliteItemEventRepository(
    private val database: KkvsDatabase
) : KkvsItemEventRepository {
    override suspend fun create(itemEvent: ItemEvent) {
        database.item_eventQueries
            .insert(Item_event(
                created_at = itemEvent.createdAt,
                item_type = itemEvent.itemType,
                item_key = itemEvent.itemKey,
                event_type = itemEvent.eventType.toEntity()
            ))
    }

    override suspend fun deleteBefore(createdAt: Long) {
        database.item_eventQueries
            .deleteBefore(createdAt)
    }
}
