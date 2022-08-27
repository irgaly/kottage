package io.github.irgaly.kottage.data.sqlite

import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kottage.data.sqlite.internal.FrameworkSQLiteOpenHelperFactory
import io.github.irgaly.kottage.platform.Context

actual class DriverFactory actual constructor(private val context: Context) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        // SQLiteOpenHelper:
        // * journal_size_limit = ? (default)
        //   * 524288 bytes = 512 KB に設定
        // * secure_delete = ? (default)
        //   * 0 = OFF に設定
        // * cache_size = ? (default)
        //   * -2000 (KB) = 2MB に設定
        // * sqlite_busy_timeout = 2500 ms (default)
        //   * https://android.googlesource.com/platform/frameworks/base.git/+/refs/heads/master/core/jni/android_database_SQLiteConnection.cpp#59
        // * threading mode = multi-thread
        //   * https://android.googlesource.com/platform/frameworks/base/+/master/core/jni/android_database_SQLiteGlobal.cpp
        //   * SQLiteDatabase で ThreadLocal Connection
        //     * https://android.googlesource.com/platform/frameworks/base.git/+/refs/heads/master/core/java/android/database/sqlite/SQLiteDatabase.java#107
        //     * 単発クエリはどのスレッドからでも呼び出し可能
        //     * Transaction 開始~終了の間では同一スレッドである必要がある
        return AndroidSqliteDriver(
            KottageDatabase.Schema,
            context.context,
            "$fileName.db",
            FrameworkSQLiteOpenHelperFactory(directoryPath),
            object : AndroidSqliteDriver.Callback(KottageDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA journal_size_limit = 524288")
                    db.execSQL("PRAGMA secure_delete = 0")
                    db.execSQL("PRAGMA cache_size = -2000")
                    db.execSQL("PRAGMA synchronous = NORMAL")
                    db.execSQL("PRAGMA busy_timeout = 3000")
                    super.onOpen(db)
                }
            }
        )
    }
}
