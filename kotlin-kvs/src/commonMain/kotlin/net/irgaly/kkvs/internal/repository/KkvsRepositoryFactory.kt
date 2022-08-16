package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.internal.database.DatabaseConnection

internal expect class KkvsRepositoryFactory(
    databaseConnection: DatabaseConnection
) {
    fun createItemRepository(itemType: String): KkvsItemRepository
    fun createItemEventRepository(): KkvsItemEventRepository
}
