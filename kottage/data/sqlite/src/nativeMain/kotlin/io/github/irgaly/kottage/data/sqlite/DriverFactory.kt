package io.github.irgaly.kottage.data.sqlite

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.SynchronousFlag
import co.touchlab.sqliter.longForQuery
import io.github.irgaly.kottage.platform.Context
import kotlinx.coroutines.CoroutineDispatcher

actual class DriverFactory actual constructor(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) {
    actual suspend fun createDriver(
        fileName: String,
        directoryPath: String,
        schema: SqlSchema<QueryResult.Value<Unit>>
    ): SqlDriver {
        // SQLiter:
        // * journal_size_limit = 32768 (default)
        //   * 524288 bytes = 512 KB に設定
        // * secure_delete = 2 (default)
        //   * 0 = OFF に設定
        // * cache_size = 2000 (pages, default)
        //   * -2000 (KB) = 2MB に設定
        // * sqlite_busy_timeout = 5000 ms (default)
        // * threading mode = multi-thread
        //   * https://github.com/touchlab/SQLiter/issues/37
        //   * SQLiter supports single connection by concurrency access with lock
        //     * https://github.com/touchlab/SQLiter/blob/main/sqliter-driver/src/nativeCommonMain/kotlin/co/touchlab/sqliter/native/NativeDatabaseManager.kt
        //     * https://github.com/touchlab/SQLiter/blob/main/sqliter-driver/src/nativeCommonMain/kotlin/co/touchlab/sqliter/concurrency/ConcurrentDatabaseConnection.kt
        return NativeSqliteDriver(
            DatabaseConfiguration(
                name = "$fileName.db",
                version = schema.version.toInt(),
                create = { connection ->
                    wrapConnection(connection) { schema.create(it) }
                },
                upgrade = { connection, oldVersion, newVersion ->
                    wrapConnection(connection) {
                        schema.migrate(it, oldVersion.toLong(), newVersion.toLong())
                    }
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
