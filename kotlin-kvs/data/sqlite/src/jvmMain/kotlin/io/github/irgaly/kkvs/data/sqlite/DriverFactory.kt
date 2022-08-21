package io.github.irgaly.kkvs.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.github.irgaly.kkvs.platform.Context
import java.util.*

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:${directoryPath}/$fileName.db",
            Properties().apply {
                put("journal_mode", "WAL")
                put("synchronous", "NORMAL")
            }
        )
        KkvsDatabase.Schema.create(driver)
        return driver
    }
}
