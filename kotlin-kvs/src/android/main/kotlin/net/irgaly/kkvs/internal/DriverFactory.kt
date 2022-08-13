package net.irgaly.kkvs.internal

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.Database
import net.irgaly.kkvs.KkvsEnvironment

internal actual class DriverFactory actual constructor(private val environment: KkvsEnvironment) {
    actual suspend fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        return AndroidSqliteDriver(
            Database.Schema,
            environment.context,
            fileName,
            FrameworkSQLiteOpenHelperFactory(directoryPath)
        )
    }
}
