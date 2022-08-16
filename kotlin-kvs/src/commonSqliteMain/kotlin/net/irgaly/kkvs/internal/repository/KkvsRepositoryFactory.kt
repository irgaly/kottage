package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.internal.database.DatabaseConnection

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
