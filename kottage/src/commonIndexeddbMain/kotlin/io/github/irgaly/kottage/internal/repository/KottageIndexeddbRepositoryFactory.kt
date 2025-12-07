package io.github.irgaly.kottage.internal.repository

/**
 * Note:
 * * indexeddb comparison
 *     * https://stackoverflow.com/a/15625231/13403244
 *     * https://w3c.github.io/IndexedDB/#key-construct
 *     * null は index に含まれない
 * * Kotlin/JS
 *     * external types https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.js/-js-export/
 * * Kotlin indexeddb
 *     * https://github.com/JuulLabs/indexeddb
 */
internal class KottageIndexeddbRepositoryFactory: KottageRepositoryFactory {
    override suspend fun createItemRepository(): KottageItemRepository {
        return KottageIndexeddbItemRepository()
    }

    override suspend fun createItemListRepository(): KottageItemListRepository {
        return KottageIndexeddbItemListRepository()
    }

    override suspend fun createItemEventRepository(): KottageItemEventRepository {
        return KottageIndexeddbItemEventRepository()
    }

    override suspend fun createStatsRepository(): KottageStatsRepository {
        return KottageIndexeddbStatsRepository()
    }
}
