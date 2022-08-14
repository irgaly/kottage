package net.irgaly.kkvs.internal.model

data class ItemEvent(
    val createdAt: Long,
    val itemType: String,
    val itemKey: String,
    val eventType: ItemEventType
)
