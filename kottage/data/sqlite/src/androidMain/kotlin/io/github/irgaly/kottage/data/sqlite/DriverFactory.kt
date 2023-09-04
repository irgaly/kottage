package io.github.irgaly.kottage.data.sqlite

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.irgaly.kottage.data.sqlite.internal.FrameworkSQLiteOpenHelperFactory
import io.github.irgaly.kottage.platform.Context
import kotlinx.coroutines.CoroutineDispatcher

actual class DriverFactory actual constructor(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) {
    actual suspend fun createDriver(
        fileName: String,
        directoryPath: String,
        schema: SqlSchema<QueryResult.Value<Unit>>
    ): SqlDriver {
        // SQLiteOpenHelper:
        // * journal_size_limit = ? (default)
        //   * 524288 bytes = 512 KB に設定
        // * wal_autocheckpoint = 100 (default)
        //   * 1000 pages に設定
        // * auto_vacuum = 1 (devault)
        //   * 0 = NONE に設定
        // * secure_delete = ? (default)
        //   * 0 = OFF に設定
        // * cache_size = ? (default)
        //   * -2000 (KB) = 2MB に設定
        // * sqlite_busy_timeout = 2500 ms (default)
        //   * https://android.googlesource.com/platform/frameworks/base.git/+/refs/heads/master/core/jni/android_database_SQLiteConnection.cpp#59
        //   * 3000 ms に設定
        // * threading mode = multi-thread
        //   * https://android.googlesource.com/platform/frameworks/base/+/master/core/jni/android_database_SQLiteGlobal.cpp
        //   * SQLiteDatabase で ThreadLocal Connection
        //     * https://android.googlesource.com/platform/frameworks/base.git/+/refs/heads/master/core/java/android/database/sqlite/SQLiteDatabase.java#107
        //     * 単発クエリはどのスレッドからでも呼び出し可能
        //     * Transaction 開始~終了の間では同一スレッドである必要がある
        return AndroidSqliteDriver(
            schema,
            context.context,
            "$fileName.db",
            FrameworkSQLiteOpenHelperFactory(directoryPath),
            object : AndroidSqliteDriver.Callback(schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.query("PRAGMA journal_size_limit = 524288").close()
                    db.query("PRAGMA wal_autocheckpoint = 1000").close()
                    db.query("PRAGMA auto_vacuum = NONE").close()
                    db.query("PRAGMA secure_delete = 0").close()
                    db.query("PRAGMA cache_size = -2000").close()
                    db.query("PRAGMA synchronous = NORMAL").close()
                    db.query("PRAGMA busy_timeout = 3000").close()
                }
            }
        )
    }
}
