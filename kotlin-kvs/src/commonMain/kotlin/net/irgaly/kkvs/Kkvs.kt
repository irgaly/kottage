package net.irgaly.kkvs

import kotlinx.serialization.json.Json
import net.irgaly.kkvs.internal.KkvsStorageImpl
import net.irgaly.kkvs.internal.repository.KkvsRepositoryFactory

/**
 * Kotlin KVS
 */
class Kkvs(
    val name: String,
    val directoryPath: String,
    val environment: KkvsEnvironment,
    val json: Json = Json
) {
    private val repositoryFactory by lazy {
        KkvsRepositoryFactory(name, directoryPath, environment)
    }

    fun storage(name: String, options: KkvsStorageOptions): KkvsStorage {
        return KkvsStorageImpl(
            name,
            options.json ?: json,
            options,
            repositoryFactory,
            environment.calendar
        )
    }
}
