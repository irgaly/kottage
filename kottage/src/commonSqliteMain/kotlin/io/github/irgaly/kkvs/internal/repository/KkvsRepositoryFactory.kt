package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.database.DatabaseConnection

internal actual class KkvsRepositoryFactory actual constructor(
    private val databaseConnection: DatabaseConnection
) {
    actual fun createItemRepository(itemType: String): KkvsItemRepository {
        return KkvsSqliteItemRepository(databaseConnection.database, itemType)
    }

    actual fun createItemEventRepository(): KkvsItemEventRepository {
        return KkvsSqliteItemEventRepository(databaseConnection.database)
    }
}
