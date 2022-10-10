package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.model.ItemEvent
import kotlinx.serialization.Serializable

@Serializable
data class KottageEvent(
    val id: String,
    val createdAt: Long,
    val expireAt: Long?,
    val itemType: String,
    val itemKey: String,
    val listType: String?,
    val listPositionId: String?,
    val eventType: KottageEventType
) {
    companion object {
        internal fun from(itemEvent: ItemEvent): KottageEvent {
            return KottageEvent(
                id = itemEvent.id,
                createdAt = itemEvent.createdAt,
                expireAt = itemEvent.expireAt,
                itemType = itemEvent.itemType,
                itemKey = itemEvent.itemKey,
                listType = itemEvent.itemListType,
                listPositionId = itemEvent.itemListId,
                eventType = KottageEventType.from(itemEvent.eventType)
            )
        }
    }
}
