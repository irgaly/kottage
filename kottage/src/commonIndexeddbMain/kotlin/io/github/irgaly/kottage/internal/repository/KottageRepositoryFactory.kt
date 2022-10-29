package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

/**
 * Note:
 * * indexeddb comparison
 *     * https://stackoverflow.com/a/15625231/13403244
 *     * https://w3c.github.io/IndexedDB/#key-construct
 *     * null は index に含まれない
 */
internal actual class KottageRepositoryFactory actual constructor(
    private val databaseConnection: DatabaseConnection
) {
    actual suspend fun createItemRepository(): KottageItemRepository {
        return KottageIndexeddbItemRepository()
    }

    actual suspend fun createItemListRepository(): KottageItemListRepository {
        return KottageIndexeddbItemListRepository()
    }

    actual suspend fun createItemEventRepository(): KottageItemEventRepository {
        return KottageIndexeddbItemEventRepository()
    }

    actual suspend fun createStatsRepository(): KottageStatsRepository {
        return KottageIndexeddbStatsRepository()
    }
}
