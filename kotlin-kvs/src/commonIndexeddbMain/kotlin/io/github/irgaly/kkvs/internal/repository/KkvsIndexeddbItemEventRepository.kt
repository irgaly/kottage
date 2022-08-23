package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.model.ItemEvent

internal class KkvsIndexeddbItemEventRepository : KkvsItemEventRepository {
    override fun create(itemEvent: ItemEvent) {
        TODO("Not yet implemented")
    }

    override fun selectAfter(itemType: String, createdAt: Long): List<ItemEvent> {
        TODO("Not yet implemented")
    }

    override fun selectAfter(createdAt: Long): List<ItemEvent> {
        TODO("Not yet implemented")
    }

    override fun deleteBefore(createdAt: Long) {
        TODO("Not yet implemented")
    }
}
