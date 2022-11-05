package io.github.irgaly.kottage.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.external.DatabaseConstructor
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
        val driver = NodejsSqlDriver(betterSqlite3Database("$directoryPath/$fileName"))
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
