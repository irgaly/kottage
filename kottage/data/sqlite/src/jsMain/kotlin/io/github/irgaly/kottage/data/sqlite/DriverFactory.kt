package io.github.irgaly.kottage.data.sqlite

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.external.DatabaseConstructor
import io.github.irgaly.kottage.data.sqlite.external.Options
import io.github.irgaly.kottage.platform.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

actual class DriverFactory actual constructor(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) {
    actual suspend fun createDriver(
        fileName: String,
        directoryPath: String,
        schema: SqlDriver.Schema
    ): SqlDriver {
        // better-sqlite3
        // * busy_timeout = 5000 (ms, default)
        //   * 3000 (ms) に設定
        // * journal_mode = DELETE (default)
        //   * DELETE に設定
        // * journal_size_limit = -1 (default)
        //   * 524288 bytes = 512 KB に設定
        // * synchronous = FULL (FULL, default)
        //   * NORMAL に設定
        // * cache_size = -16000 (KB, default)
        //   * -2000 (KB) に設定
        // * threading mode = multi-thread
        //   * https://github.com/WiseLibs/better-sqlite3/blob/d66747ef563dc39fd038e0ec60d91c9084e3e62c/docs/compilation.md
        //   * JavaScript なのでシングルスレッドであり serialized は不要
        val driver = NodejsSqlDriver(
            betterSqlite3Database(
                "$directoryPath/$fileName.db",
                js("{}").unsafeCast<Options>().apply {
                    timeout = 3000
                }
            )
        ).apply {
            db.pragma("journal_mode = WAL")
            db.pragma("journal_size_limit = 524288")
            db.pragma("synchronous = NORMAL")
            db.pragma("cache_size = -2000")
        }
        migrateIfNeeded(driver, schema)
        return driver
    }

    private suspend fun migrateIfNeeded(driver: SqlDriver, schema: SqlDriver.Schema) {
        withContext(dispatcher) {
            val oldVersion = driver.executeQuery(null, "PRAGMA user_version", 0).use { cursor ->
                if (cursor.next()) {
                    cursor.getLong(0)?.toInt()
                } else null
            } ?: 0
            val newVersion = schema.version
            if (oldVersion == 0) {
                schema.create(driver)
                driver.execute(null, "PRAGMA user_version = $newVersion", 0)
            } else if (oldVersion < newVersion) {
                // migrate oldVersion -> newVersion
                schema.migrate(driver, oldVersion, newVersion)
                driver.execute(null, "PRAGMA user_version = $newVersion", 0)
            }
        }
    }
}

private val betterSqlite3Database: DatabaseConstructor
    get() = js("require('better-sqlite3')").unsafeCast<DatabaseConstructor>()
