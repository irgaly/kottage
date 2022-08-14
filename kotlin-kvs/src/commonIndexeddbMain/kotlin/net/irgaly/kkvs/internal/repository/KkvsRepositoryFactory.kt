package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.KkvsEnvironment

internal actual class KkvsRepositoryFactory actual constructor(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    actual fun <R> transactionWithResult(bodyWithReturn: () -> R): R {
        TODO("Not yet implemented")
    }

    actual fun transaction(body: () -> Unit) {
        TODO("Not yet implemented")
    }

    actual fun createItemRepository(itemType: String): KkvsItemRepository {
        return KkvsIndexeddbItemRepository(itemType)
    }

    actual fun createItemEventRepository(): KkvsItemEventRepository {
        return KkvsIndexeddbItemEventRepository()
    }
}
