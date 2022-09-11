package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.ItemEvent

internal interface KottageItemEventRepository {
    fun create(itemEvent: ItemEvent)
    fun selectAfter(
        itemType: String,
        createdAt: Long,
        limit: Long?
    ): List<ItemEvent>

    fun getLatestCreatedAt(itemType: String): Long?
    fun deleteBefore(createdAt: Long)

}
