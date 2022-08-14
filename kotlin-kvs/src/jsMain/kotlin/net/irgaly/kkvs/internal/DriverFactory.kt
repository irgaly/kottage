package net.irgaly.kkvs.internal

import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.KkvsEnvironment

internal actual class DriverFactory actual constructor(private val environment: KkvsEnvironment) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        throw NotImplementedError()
    }
}
