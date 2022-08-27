package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.ItemEvent

internal interface KkvsItemEventRepository {
    fun create(itemEvent: ItemEvent)
    fun selectAfter(itemType: String, createdAt: Long): List<ItemEvent>
    fun selectAfter(createdAt: Long): List<ItemEvent>
    fun deleteBefore(createdAt: Long)
}
