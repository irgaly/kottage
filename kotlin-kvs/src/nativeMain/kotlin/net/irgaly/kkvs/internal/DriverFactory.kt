package net.irgaly.kkvs.internal

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import net.irgaly.kkvs.Database
import net.irgaly.kkvs.KkvsEnvironment

internal actual class DriverFactory actual constructor(private val environment: KkvsEnvironment) {
    actual suspend fun createDriver(fileName: String, directory: String): SqlDriver {
        return NativeSqliteDriver(Database.Schema, fileName)
    }
}
