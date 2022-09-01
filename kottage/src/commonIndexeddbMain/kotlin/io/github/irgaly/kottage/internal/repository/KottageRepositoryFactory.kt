package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

internal actual class KottageRepositoryFactory actual constructor(
    databaseConnection: DatabaseConnection
) {

    actual suspend fun createItemRepository(): KottageItemRepository {
        return KottageIndexeddbItemRepository()
    }

    actual suspend fun createItemEventRepository(): KottageItemEventRepository {
        return KottageIndexeddbItemEventRepository()
    }
}
