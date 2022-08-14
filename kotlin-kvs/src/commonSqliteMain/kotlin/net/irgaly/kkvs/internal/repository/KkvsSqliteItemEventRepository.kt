package net.irgaly.kkvs.internal.repository

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.data.sqlite.Item_event
import net.irgaly.kkvs.data.sqlite.KkvsDatabase
import net.irgaly.kkvs.internal.model.ItemEvent

class KkvsSqliteItemEventRepository(
    private val driver: SqlDriver
) : KkvsItemEventRepository {
    private val database: KkvsDatabase by lazy {
        KkvsDatabase(driver, Item_event.Adapter(EnumColumnAdapter()))
    }

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
