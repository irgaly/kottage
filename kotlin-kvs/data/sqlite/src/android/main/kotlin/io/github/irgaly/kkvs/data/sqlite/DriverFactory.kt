package io.github.irgaly.kkvs.data.sqlite

import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kkvs.data.sqlite.internal.FrameworkSQLiteOpenHelperFactory
import io.github.irgaly.kkvs.platform.Context

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        // SQLiteOpenHelper:
        // * sqlite_busy_timeout = 2500 ms (default)
        //   * https://android.googlesource.com/platform/frameworks/base.git/+/refs/heads/master/core/jni/android_database_SQLiteConnection.cpp#59
        // * threading mode = multi-thread
        //   * https://android.googlesource.com/platform/frameworks/base/+/master/core/jni/android_database_SQLiteGlobal.cpp
        //   * SQLiteDatabase で ThreadLocal Connection
        //     * https://android.googlesource.com/platform/frameworks/base.git/+/refs/heads/master/core/java/android/database/sqlite/SQLiteDatabase.java#107
        //     * 単発クエリはどのスレッドからでも呼び出し可能
        //     * Transaction 開始~終了の間では同一スレッドである必要がある
        return AndroidSqliteDriver(
            KkvsDatabase.Schema,
            context.context,
            "$fileName.db",
            FrameworkSQLiteOpenHelperFactory(directoryPath),
            object : AndroidSqliteDriver.Callback(KkvsDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA synchronous = NORMAL")
                    db.execSQL("PRAGMA busy_timeout = 3000")
                    super.onOpen(db)
                }
            }
        )
    }
}
