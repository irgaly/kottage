package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

internal actual class KottageRepositoryFactory actual constructor(
    databaseConnection: DatabaseConnection
) {

    actual fun createItemRepository(itemType: String): KottageItemRepository {
        return KottageIndexeddbItemRepository(itemType)
    }

    actual fun createItemEventRepository(): KottageItemEventRepository {
        return KottageIndexeddbItemEventRepository()
    }
}
