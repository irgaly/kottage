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
    fun getCount(itemType: String): Long
    fun deleteOlderEvents(itemType: String, limit: Long)
    fun deleteBefore(createdAt: Long)
    fun deleteAll(itemType: String)
    fun getStatsCount(itemType: String): Long
    fun incrementStatsCount(itemType: String, count: Long)
    fun decrementStatsCount(itemType: String, count: Long)
    fun updateStatsCount(itemType: String, count: Long)
}
