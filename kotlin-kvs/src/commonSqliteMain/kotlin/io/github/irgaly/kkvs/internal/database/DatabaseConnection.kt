package io.github.irgaly.kkvs.internal.database

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use
import io.github.irgaly.kkvs.KkvsEnvironment
import io.github.irgaly.kkvs.data.sqlite.DriverFactory
import io.github.irgaly.kkvs.data.sqlite.Item_event
import io.github.irgaly.kkvs.data.sqlite.KkvsDatabase
import io.github.irgaly.kkvs.platform.Files
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
internal actual data class DatabaseConnection(
    val sqlDriver: SqlDriver,
    val database: KkvsDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    actual suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R =
        withContext(dispatcher) {
            // SQLDelight Transaction = DEFERRED (default)
            database.transactionWithResult {
                // restart transaction with EXCLUSIVE
                sqlDriver.execute(null, "END", 0)
                sqlDriver.execute(null, "BEGIN EXCLUSIVE", 0)
                bodyWithReturn()
            }
        }

    actual suspend fun transaction(body: () -> Unit) = withContext(dispatcher) {
        // SQLDelight Transaction = DEFERRED (default)
        database.transaction {
            // restart transaction with EXCLUSIVE
            sqlDriver.execute(null, "END", 0)
            sqlDriver.execute(null, "BEGIN EXCLUSIVE", 0)
            body()
        }
    }

    actual suspend fun deleteAll() = withContext(dispatcher) {
        database.transaction {
            database.itemQueries.deleteAll()
        }
    }

    actual suspend fun compact() = withContext(dispatcher) {
        // reduce WAL file size to zero / https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
        // TODO: JDBC -> .execute(), Native -> executeQuery() で実行する
        sqlDriver.executeQuery(null, "PRAGMA wal_checkpoint(TRUNCATE)", 0)
        // reduce database file size and optimize b-tree / https://www.sqlite.org/matrix/lang_vacuum.html
        sqlDriver.execute(null, "VACUUM", 0)
    }

    actual suspend fun backupTo(file: String, directoryPath: String) = withContext(dispatcher) {
        if (!Files.exists(directoryPath)) {
            Files.mkdirs(directoryPath)
        }
        val destination = "$directoryPath/$file"
        // .delete は sqlite3 コマンドのためSQL経由では使えない
        sqlDriver.execute(null, "VACUUM INTO '${destination.replace("'", "''")}'", 0)
    }

    actual suspend fun getDatabaseStatus(): String = withContext(dispatcher) {
        database.transactionWithResult {
            //val userVersion = database.pragmaQueries.getUserVersion()
            //val journalMode = database.pragmaQueries.getJournalMode()
            //val synchronous = database.pragmaQueries.getSynchronous()
            //val autoVacuum = database.pragmaQueries.getAutoVacuum()
            //val lockingMode = database.pragmaQueries.getLockingMode()
            // Good: WAL + synchronous = NORMAL(1)
            val journalMode = sqlDriver.executeQuery(null, "PRAGMA journal_mode", 0).use {
                if (it.next()) {
                    it.getString(0)
                } else null
            }
            val journalSizeLimit =
                sqlDriver.executeQuery(null, "PRAGMA journal_size_limit", 0).use {
                    if (it.next()) {
                        it.getLong(0)
                    } else null
                }
            val walAutoCheckpoint =
                sqlDriver.executeQuery(null, "PRAGMA wal_autocheckpoint", 0).use {
                    if (it.next()) {
                        it.getLong(0)
                    } else null
                }
            val synchronous = sqlDriver.executeQuery(null, "PRAGMA synchronous", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val tempStore = sqlDriver.executeQuery(null, "PRAGMA temp_store", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val memoryMapSize = sqlDriver.executeQuery(null, "PRAGMA mmap_size", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val autoVacuum = sqlDriver.executeQuery(null, "PRAGMA auto_vacuum", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val lockingMode = sqlDriver.executeQuery(null, "PRAGMA locking_mode", 0).use {
                if (it.next()) {
                    it.getString(0)
                } else null
            }
            val maxPageCount = sqlDriver.executeQuery(null, "PRAGMA max_page_count", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val cacheSize = sqlDriver.executeQuery(null, "PRAGMA cache_size", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val pageSize = sqlDriver.executeQuery(null, "PRAGMA page_size", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val busyTimeout = sqlDriver.executeQuery(null, "PRAGMA busy_timeout", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val secureDelete = sqlDriver.executeQuery(null, "PRAGMA secure_delete", 0).use {
                if (it.next()) {
                    it.getString(0)
                } else null
            }
            val userVersion = sqlDriver.executeQuery(null, "PRAGMA user_version", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val freelistCount = sqlDriver.executeQuery(null, "PRAGMA freelist_count", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            """
                --- configs:
                journal_mode = $journalMode (DELETE | TRUNCATE | PERSIST | MEMORY | WAL | OFF)
                journal_size_limit = $journalSizeLimit (bytes, negative = no limit, 0 = truncate to minimum)
                wal_autocheckpoint = $walAutoCheckpoint (pages)
                synchronous = $synchronous (0 = OFF, 1 = NORMAL, 2 = FULL, 3 = EXTRA)
                temp_store = $tempStore (0 = DEFAULT, 1 = FILE, 2 = MEMORY)
                mmap_size = $memoryMapSize (bytes)
                auto_vacuum = $autoVacuum (0 = NONE, 1 = FULL, 2 = INCREMENTAL)
                locking_mode = $lockingMode (NORMAL | EXCLUSIVE)
                max_page_count = $maxPageCount (pages)
                cache_size = $cacheSize (negative = KiB, positive = pages)
                page_size = $pageSize (bytes)
                busy_timeout = $busyTimeout (milliseconds)
                secure_delete = $secureDelete (0 = OFF, 1 = ON, 2 = FAST)
                --- stats:
                user_version = $userVersion
                freelist_count = $freelistCount (pages)
            """.trimIndent()
        }
    }
}

internal actual fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
    dispatcher: CoroutineDispatcher
): DatabaseConnection {
    val driver = DriverFactory(environment.context).createDriver(fileName, directoryPath)
    val database = KkvsDatabase(driver, Item_event.Adapter(EnumColumnAdapter()))
    return DatabaseConnection(driver, database, dispatcher)
}
