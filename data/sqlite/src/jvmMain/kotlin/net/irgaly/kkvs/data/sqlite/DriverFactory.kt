package net.irgaly.kkvs.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import net.irgaly.kkvs.platform.Context

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${directoryPath}/$fileName")
        KkvsDatabase.Schema.create(driver)
        return driver
    }
}
