package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.KkvsEnvironment

internal expect class KkvsRepositoryFactory(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    fun <R> transactionWithResult(bodyWithReturn: () -> R): R
    fun transaction(body: () -> Unit)
    fun createItemRepository(itemType: String): KkvsItemRepository
    fun createItemEventRepository(): KkvsItemEventRepository
}
