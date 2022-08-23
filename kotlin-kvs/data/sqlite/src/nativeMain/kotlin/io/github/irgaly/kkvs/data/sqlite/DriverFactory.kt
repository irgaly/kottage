package io.github.irgaly.kkvs.data.sqlite

import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.SynchronousFlag
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection
import io.github.irgaly.kkvs.platform.Context

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        // SQLiter:
        // * sqlite_busy_timeout = 5000 ms (default)
        // * threading mode = multi-thread
        //   * SQLiter supports single connection by concurrency access with lock
        //     * https://github.com/touchlab/SQLiter/blob/main/sqliter-driver/src/nativeCommonMain/kotlin/co/touchlab/sqliter/native/NativeDatabaseManager.kt
        //     * https://github.com/touchlab/SQLiter/blob/main/sqliter-driver/src/nativeCommonMain/kotlin/co/touchlab/sqliter/concurrency/ConcurrentDatabaseConnection.kt
        val schema = KkvsDatabase.Schema
        return NativeSqliteDriver(
            DatabaseConfiguration(
                name = "$fileName.db",
                version = schema.version,
                create = { connection ->
                    wrapConnection(connection) { schema.create(it) }
                },
                upgrade = { connection, oldVersion, newVersion ->
                    wrapConnection(connection) { schema.migrate(it, oldVersion, newVersion) }
                },
                inMemory = false,
                journalMode = JournalMode.WAL,
                extendedConfig = DatabaseConfiguration.Extended(
                    busyTimeout = 3000,
                    basePath = directoryPath,
                    synchronousFlag = SynchronousFlag.NORMAL
                )
            ),
        )
    }
}
