package net.irgaly.kkvs.internal

import net.irgaly.kkvs.KkvsEnvironment

internal expect class KkvsRepositoryFactory() {
    fun create(
        itemType: String,
        fileName: String,
        directoryPath: String,
        environment: KkvsEnvironment
    ): KkvsRepository
}
