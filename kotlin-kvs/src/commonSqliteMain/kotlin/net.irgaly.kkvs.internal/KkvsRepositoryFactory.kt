package net.irgaly.kkvs.internal

import net.irgaly.kkvs.KkvsEnvironment

internal actual class KkvsRepositoryFactory {
    actual fun create(
        itemType: String,
        fileName: String,
        directoryPath: String,
        environment: KkvsEnvironment
    ): KkvsRepository {
        return KkvsSqliteRepository(
            itemType = itemType,
            fileName = fileName,
            directoryPath = directoryPath,
            environment = environment
        )
    }
}
