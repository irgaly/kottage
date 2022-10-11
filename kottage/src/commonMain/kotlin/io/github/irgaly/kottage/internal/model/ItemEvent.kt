package io.github.irgaly.kottage.internal.model

internal data class ItemEvent(
    val id: String,
    val createdAt: Long,
    val expireAt: Long?,
    val itemType: String,
    val itemKey: String,
    val itemListId: String?,
    val itemListType: String?,
    val eventType: ItemEventType
)
