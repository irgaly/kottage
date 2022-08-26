package io.github.irgaly.kkvs.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.github.irgaly.kkvs.platform.Context
import java.util.*

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        // SQLDelight + SQLiter + JDBC:
        // * journal_size_limit = -1 (default)
        //   * 524288 bytes = 512 KB に設定
        // * secure_delete = 0 (default)
        //   * そのまま
        // * cache_size = -2000 (KB, default)
        //   * そのまま
        // * sqlite_busy_timeout = 3000 (default)
        //   * https://github.com/xerial/sqlite-jdbc/blob/14d59032fb5dc691e48877ecde783719b7657fba/src/main/java/org/sqlite/SQLiteConfig.java
        // * threading mode = multi-thread
        //   * https://github.com/xerial/sqlite-jdbc/issues/199
        //   * ThreadedConnectionManager で ThreadLocal Connection
        //     * https://github.com/cashapp/sqldelight/blob/cb699fcc19632a70deeb2930470bcf09db625a42/drivers/sqlite-driver/src/main/kotlin/app/cash/sqldelight/driver/jdbc/sqlite/JdbcSqliteDriver.kt#L83
        //     * 単発クエリはどのスレッドからでも呼び出し可能
        //     * Transaction 開始~終了の間では同一スレッドである必要がある
        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:${directoryPath}/$fileName.db",
            Properties().apply {
                put("journal_mode", "WAL")
                put("journal_size_limit", "524288")
                put("synchronous", "NORMAL")
                put("busy_timeout", "3000")
            }
        )
        // TODO: do async initialization (or lazy initialization)
        migrateIfNeeded(driver)
        return driver
    }

    private fun migrateIfNeeded(driver: JdbcSqliteDriver) {
        val oldVersion = driver.executeQuery(null, "PRAGMA user_version", 0).use { cursor ->
            if (cursor.next()) {
                cursor.getLong(0)?.toInt()
            } else {
                null
            }
        } ?: 0
        val newVersion = KkvsDatabase.Schema.version
        if (oldVersion == 0) {
            KkvsDatabase.Schema.create(driver)
            driver.execute(null, "PRAGMA user_version=$newVersion", 0)
        } else if (oldVersion < newVersion) {
            // migrate oldVersion -> newVersion
            KkvsDatabase.Schema.migrate(driver, oldVersion, newVersion)
            driver.execute(null, "PRAGMA user_version=$newVersion", 0)
        }
    }
}
