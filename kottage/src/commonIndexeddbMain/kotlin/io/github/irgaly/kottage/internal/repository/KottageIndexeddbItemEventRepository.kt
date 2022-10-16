package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.ItemEvent

internal class KottageIndexeddbItemEventRepository : KottageItemEventRepository {
    override fun create(itemEvent: ItemEvent) {
        TODO("Not yet implemented")
    }

    override fun selectAfter(createdAt: Long, itemType: String?, limit: Long?): List<ItemEvent> {
        TODO("Not yet implemented")
    }

    override fun getLatestCreatedAt(itemType: String): Long? {
        TODO("Not yet implemented")
    }

    override fun getExpiredIds(
        now: Long,
        itemType: String?,
        receiver: (id: String, itemType: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getCount(itemType: String): Long {
        TODO("Not yet implemented")
    }

    override fun delete(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteOlderEvents(itemType: String, limit: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteBefore(createdAt: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(itemType: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAllList(listType: String) {
        TODO("Not yet implemented")
    }

    override fun getStatsCount(itemType: String): Long {
        TODO("Not yet implemented")
    }

    override fun incrementStatsCount(itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun decrementStatsCount(itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsCount(itemType: String, count: Long) {
        TODO("Not yet implemented")
    }
}
