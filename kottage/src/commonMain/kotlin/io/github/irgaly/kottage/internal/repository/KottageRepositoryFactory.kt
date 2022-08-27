package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

internal expect class KottageRepositoryFactory(
    databaseConnection: DatabaseConnection
) {
    fun createItemRepository(itemType: String): KottageItemRepository
    fun createItemEventRepository(): KottageItemEventRepository
}
