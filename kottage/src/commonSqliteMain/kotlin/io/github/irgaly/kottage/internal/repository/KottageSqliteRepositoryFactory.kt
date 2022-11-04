package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.database.SqliteDatabaseConnection

internal class KottageSqliteRepositoryFactory(
    private val databaseConnection: DatabaseConnection
) : KottageRepositoryFactory {
    override suspend fun createItemRepository(): KottageItemRepository {
        return KottageSqliteItemRepository((databaseConnection as SqliteDatabaseConnection).database.await())
    }

    override suspend fun createItemListRepository(): KottageItemListRepository {
        return KottageSqliteItemListRepository((databaseConnection as SqliteDatabaseConnection).database.await())
    }

    override suspend fun createItemEventRepository(): KottageItemEventRepository {
        return KottageSqliteItemEventRepository((databaseConnection as SqliteDatabaseConnection).database.await())
    }

    override suspend fun createStatsRepository(): KottageStatsRepository {
        return KottageSqliteStatsRepository((databaseConnection as SqliteDatabaseConnection).database.await())
    }
}
