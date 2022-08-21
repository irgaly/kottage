package io.github.irgaly.kkvs.data.sqlite

import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kkvs.data.sqlite.internal.FrameworkSQLiteOpenHelperFactory
import io.github.irgaly.kkvs.platform.Context

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        return AndroidSqliteDriver(
            KkvsDatabase.Schema,
            context.context,
            "$fileName.db",
            FrameworkSQLiteOpenHelperFactory(directoryPath),
            object : AndroidSqliteDriver.Callback(KkvsDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA synchronous = NORMAL")
                    super.onOpen(db)
                }
            }
        )
    }
}
