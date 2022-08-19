package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.model.ItemEvent

internal interface KkvsItemEventRepository {
    suspend fun create(itemEvent: ItemEvent)
    suspend fun selectAfter(itemType: String, createdAt: Long): List<ItemEvent>
    suspend fun selectAfter(createdAt: Long): List<ItemEvent>
    suspend fun deleteBefore(createdAt: Long)
}