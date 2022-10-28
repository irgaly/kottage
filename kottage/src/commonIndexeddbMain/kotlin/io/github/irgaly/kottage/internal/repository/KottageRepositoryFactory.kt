package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

internal actual class KottageRepositoryFactory actual constructor(
    private val databaseConnection: DatabaseConnection
) {
    actual suspend fun createItemRepository(): KottageItemRepository {
        return KottageIndexeddbItemRepository(databaseConnection.database.await())
    }

    actual suspend fun createItemListRepository(): KottageItemListRepository {
        return KottageIndexeddbItemListRepository(databaseConnection.database.await())
    }

    actual suspend fun createItemEventRepository(): KottageItemEventRepository {
        return KottageIndexeddbItemEventRepository(databaseConnection.database.await())
    }

    actual suspend fun createStatsRepository(): KottageStatsRepository {
        return KottageIndexeddbStatsRepository(databaseConnection.database.await())
    }
}
