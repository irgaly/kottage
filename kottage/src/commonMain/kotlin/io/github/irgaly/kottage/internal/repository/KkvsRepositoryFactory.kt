package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

internal expect class KkvsRepositoryFactory(
    databaseConnection: DatabaseConnection
) {
    fun createItemRepository(itemType: String): KkvsItemRepository
    fun createItemEventRepository(): KkvsItemEventRepository
}
