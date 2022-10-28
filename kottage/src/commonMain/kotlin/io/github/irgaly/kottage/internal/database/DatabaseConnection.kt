package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.KottageEnvironment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal expect class DatabaseConnection {
    /**
     * Exclusive Transaction
     */
    suspend fun <R> transactionWithResult(bodyWithReturn: suspend Transaction.() -> R): R

    /**
     * Exclusive Transaction
     */
    suspend fun transaction(body: suspend Transaction.() -> Unit)


    /**
     * Delete all records from all tables
     *
     * * this does not drop tables
     */
    suspend fun deleteAll()

    /**
     * reduce database file
     */
    suspend fun compact()
    suspend fun backupTo(file: String, directoryPath: String)
    suspend fun getDatabaseStatus(): String
}

/**
 * @throws IllegalArgumentException invalid fileName: contains file separator
 */
@Throws(IllegalArgumentException::class)
internal expect fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KottageEnvironment,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): DatabaseConnection

internal expect suspend fun createOldDatabase(
    fileName: String,
    directoryPath: String,
    environment: KottageEnvironment,
    version: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
)
