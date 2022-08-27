package io.github.irgaly.kottage.internal.model

internal data class ItemEvent(
    val createdAt: Long,
    val itemType: String,
    val itemKey: String,
    val eventType: ItemEventType
)