package net.irgaly.kkvs.internal

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import net.irgaly.kkvs.Database
import net.irgaly.kkvs.KkvsEnvironment

internal actual class DriverFactory actual constructor(private val environment: KkvsEnvironment) {
    actual suspend fun createDriver(fileName: String, directory: String): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${directory}/$fileName")
        Database.Schema.create(driver)
        return driver
    }
}
