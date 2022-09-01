package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

internal actual class KottageRepositoryFactory actual constructor(
    private val databaseConnection: DatabaseConnection
) {
    actual fun createItemRepository(): KottageItemRepository {
        return KottageSqliteItemRepository(databaseConnection.database)
    }

    actual fun createItemEventRepository(): KottageItemEventRepository {
        return KottageSqliteItemEventRepository(databaseConnection.database)
    }
}
