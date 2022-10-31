package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.DatabaseConnection

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
 *     * v0.3.0 Issue
 *         * cursor Flow の collect 内で他の indexxeddb 非同期処理を実行すると
 *           collect 処理が最後まで進む前に cursor.continue() が実行され、次の collect 処理が並列で走ってしまう
 *         * Transaction 内の並列処理は整合性が保てないため、cursor Flow 内では非同期処理を使わないようにする
 *         * 例外として: cursor.delete() だけの実行なら許可する
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
