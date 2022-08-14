package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.KkvsEnvironment

internal expect class KkvsRepositoryFactory(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    fun create(itemType: String): KkvsItemRepository
}
