package net.irgaly.kkvs.internal

import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.KkvsEnvironment

internal expect class DriverFactory constructor(environment: KkvsEnvironment) {
    suspend fun createDriver(fileName: String, directory: String): SqlDriver
}
