package net.irgaly.kkvs.internal

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.sqljs.initSqlDriver
import kotlinx.coroutines.await
import net.irgaly.kkvs.Database
import net.irgaly.kkvs.KkvsEnvironment

internal actual class DriverFactory actual constructor(private val environment: KkvsEnvironment) {
    actual suspend fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        return initSqlDriver(Database.Schema).await()
    }
}
