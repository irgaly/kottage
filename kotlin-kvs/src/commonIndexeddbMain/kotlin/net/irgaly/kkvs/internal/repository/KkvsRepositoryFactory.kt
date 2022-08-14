package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.KkvsEnvironment

internal actual class KkvsRepositoryFactory actual constructor(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    actual fun create(itemType: String): KkvsItemRepository {
        return KkvsIndexeddbItemRepository(itemType)
    }
}
