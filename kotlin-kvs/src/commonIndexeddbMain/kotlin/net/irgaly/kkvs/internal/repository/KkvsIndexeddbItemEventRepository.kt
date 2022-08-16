package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.internal.model.ItemEvent

internal class KkvsIndexeddbItemEventRepository : KkvsItemEventRepository {
    override suspend fun create(itemEvent: ItemEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun selectAfter(itemType: String, createdAt: Long): List<ItemEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun selectAfter(createdAt: Long): List<ItemEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBefore(createdAt: Long) {
        TODO("Not yet implemented")
    }
}
