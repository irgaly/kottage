package io.github.irgaly.kkvs.data.sqlite

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kkvs.data.sqlite.internal.FrameworkSQLiteOpenHelperFactory
import io.github.irgaly.kkvs.platform.Context

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
