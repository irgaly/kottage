package io.github.irgaly.kkvs.data.sqlite.extension

import com.squareup.sqldelight.db.SqlDriver


/**
 * execute "PRAGMA wal_checkpoint(TRUNCATE)"
 *
 * * JDBC: execute()
 *     * JDBC で executeQuery wal_checkpoint を実行すると以下のエラーが発生するため、execute() を使う
 *     * org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (cannot VACUUM - SQL statements in progress)
 * * Native: executeQuery()
 * * Android: executeQuery()
 */
expect fun SqlDriver.executeWalCheckpointTruncate()
