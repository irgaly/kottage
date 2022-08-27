package io.github.irgaly.kkvs.data.sqlite

import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.SynchronousFlag
import co.touchlab.sqliter.longForQuery
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection
import io.github.irgaly.kkvs.platform.Context

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        // SQLiter:
        // * journal_size_limit = 32768 (default)
        //   * 524288 bytes = 512 KB に設定
        // * secure_delete = 2 (default)
        //   * 0 = OFF に設定
        // * cache_size = 2000 (pages, default)
        //   * -2000 (KB) = 2MB に設定
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
                ),
                lifecycleConfig = DatabaseConfiguration.Lifecycle(
                    onCreateConnection = { connection ->
                        connection.longForQuery("PRAGMA journal_size_limit = 524288")
                        connection.longForQuery("PRAGMA secure_delete = 0")
                        connection.longForQuery("PRAGMA cache_size = -2000")
                    }
                )
            )
        )
    }
}
