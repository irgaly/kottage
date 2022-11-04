package io.github.irgaly.kottage.internal.repository

internal interface KottageRepositoryFactory {
    suspend fun createItemRepository(): KottageItemRepository
    suspend fun createItemListRepository(): KottageItemListRepository
    suspend fun createItemEventRepository(): KottageItemEventRepository
    suspend fun createStatsRepository(): KottageStatsRepository
}
