package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

internal actual class KottageRepositoryFactory actual constructor(
    private val databaseConnection: DatabaseConnection
) {
    actual suspend fun createItemRepository(): KottageItemRepository {
        return KottageSqliteItemRepository(databaseConnection.database.await())
    }

    actual suspend fun createItemEventRepository(): KottageItemEventRepository {
        return KottageSqliteItemEventRepository(databaseConnection.database.await())
    }

    actual suspend fun createStatsRepository(): KottageStatsRepository {
        return KottageSqliteStatsRepository(databaseConnection.database.await())
    }
}
