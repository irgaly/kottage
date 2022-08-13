package net.irgaly.kkvs

import net.irgaly.kkvs.internal.DriverFactory

/**
 * Kotlin KVS
 */
class Kkvs(
    val name: String,
    val directory: String
) {
    companion object {
        lateinit var environment: KkvsEnvironment
        fun initialize(environment: KkvsEnvironment) {
            this.environment = environment
        }
    }

    init {
        val driver = DriverFactory(environment)
    }
}
