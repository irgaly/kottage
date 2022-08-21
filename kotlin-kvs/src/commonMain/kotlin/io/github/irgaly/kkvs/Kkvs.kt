package io.github.irgaly.kkvs

import io.github.irgaly.kkvs.internal.KkvsDatabaseManager
import io.github.irgaly.kkvs.internal.KkvsStorageImpl
import kotlinx.serialization.json.Json

/**
 * Kotlin KVS
 */
class Kkvs(
    val name: String,
    val directoryPath: String,
    val environment: KkvsEnvironment,
    val json: Json = Json
) {
    companion object {
        /**
         * Get Sqlite database file path for delete or backup a file.
         */
        fun getDatabaseFilePath(name: String, directoryPath: String): String {
            return "${directoryPath}/${name}.db"
        }
    }

    private val databaseManager by lazy {
        KkvsDatabaseManager(name, directoryPath, environment)
    }

    fun storage(name: String, options: KkvsStorageOptions): KkvsStorage {
        return KkvsStorageImpl(
            name,
            options.json ?: json,
            options,
            databaseManager,
            environment.calendar
        )
    }

    suspend fun clear() {
        databaseManager.deleteAll()
    }
}
