package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.database.DatabaseConnection

internal actual class KkvsRepositoryFactory actual constructor(
    databaseConnection: DatabaseConnection
) {

    actual fun createItemRepository(itemType: String): KkvsItemRepository {
        return KkvsIndexeddbItemRepository(itemType)
    }

    actual fun createItemEventRepository(): KkvsItemEventRepository {
        return KkvsIndexeddbItemEventRepository()
    }
}