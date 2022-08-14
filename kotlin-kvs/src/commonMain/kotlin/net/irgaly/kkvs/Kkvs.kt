package net.irgaly.kkvs

import kotlinx.serialization.json.Json
import net.irgaly.kkvs.internal.DriverFactory
import net.irgaly.kkvs.internal.KkvsSqliteRepository
import net.irgaly.kkvs.internal.KkvsStorageImpl

/**
 * Kotlin KVS
 */
class Kkvs(
    val name: String,
    val directoryPath: String,
    val json: Json = Json
) {
    companion object {
        lateinit var environment: KkvsEnvironment
        fun initialize(environment: KkvsEnvironment) {
            this.environment = environment
        }
    }

    private val driver by lazy {
        DriverFactory(environment).createDriver(name, directoryPath)
    }

    fun storage(name: String, options: KkvsStorageOptions): KkvsStorage {
        return KkvsStorageImpl(
            name,
            options,
            KkvsSqliteRepository(name, driver),
            environment.calendar
        )
    }
}
