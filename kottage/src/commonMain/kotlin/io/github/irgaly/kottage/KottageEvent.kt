package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.model.ItemEvent

data class KottageEvent(
    val id: String,
    val createdAt: Long,
    val itemType: String,
    val itemKey: String,
    val eventType: KottageEventType
) {
    companion object {
        internal fun from(itemEvent: ItemEvent): KottageEvent {
            return KottageEvent(
                itemEvent.id,
                itemEvent.createdAt,
                itemEvent.itemType,
                itemEvent.itemKey,
                KottageEventType.from(itemEvent.eventType)
            )
        }
    }
}
