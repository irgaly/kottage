package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.KkvsEnvironment

internal expect class KkvsRepositoryFactory(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    suspend fun <R> transactionWithResult(bodyWithReturn: suspend () -> R): R
    suspend fun transaction(body: suspend () -> Unit)
    fun createItemRepository(itemType: String): KkvsItemRepository
    fun createItemEventRepository(): KkvsItemEventRepository
}
