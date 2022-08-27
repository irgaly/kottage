package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.database.DatabaseConnection

internal expect class KkvsRepositoryFactory(
    databaseConnection: DatabaseConnection
) {
    fun createItemRepository(itemType: String): KkvsItemRepository
    fun createItemEventRepository(): KkvsItemEventRepository
}
