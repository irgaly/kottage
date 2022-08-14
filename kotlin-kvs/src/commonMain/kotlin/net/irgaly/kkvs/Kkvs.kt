package net.irgaly.kkvs

import kotlinx.serialization.json.Json
import net.irgaly.kkvs.internal.KkvsRepositoryFactory
import net.irgaly.kkvs.internal.KkvsStorageImpl

/**
 * Kotlin KVS
 */
class Kkvs(
    val name: String,
    val directoryPath: String,
    val environment: KkvsEnvironment,
    val json: Json = Json
) {
    fun storage(name: String, options: KkvsStorageOptions): KkvsStorage {
        return KkvsStorageImpl(
            name,
            options,
            KkvsRepositoryFactory().create(name, this.name, directoryPath, environment),
            environment.calendar
        )
    }
}
