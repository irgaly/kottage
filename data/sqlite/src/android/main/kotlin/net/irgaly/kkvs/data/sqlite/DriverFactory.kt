package net.irgaly.kkvs.data.sqlite

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.data.sqlite.internal.FrameworkSQLiteOpenHelperFactory
import net.irgaly.kkvs.platform.Context

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        return AndroidSqliteDriver(
            KkvsDatabase.Schema,
            context.context,
            fileName,
            FrameworkSQLiteOpenHelperFactory(directoryPath)
        )
    }
}
