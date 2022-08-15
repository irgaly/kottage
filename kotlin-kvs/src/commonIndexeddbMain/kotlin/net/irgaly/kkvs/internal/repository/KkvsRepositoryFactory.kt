package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.KkvsEnvironment

internal actual class KkvsRepositoryFactory actual constructor(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    actual suspend fun <R> transactionWithResult(bodyWithReturn: suspend () -> R): R {
        TODO("Not yet implemented")
    }

    actual suspend fun transaction(body: suspend () -> Unit) {
        TODO("Not yet implemented")
    }

    actual fun createItemRepository(itemType: String): KkvsItemRepository {
        return KkvsIndexeddbItemRepository(itemType)
    }

    actual fun createItemEventRepository(): KkvsItemEventRepository {
        return KkvsIndexeddbItemEventRepository()
    }
}
