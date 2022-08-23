package io.github.irgaly.kkvs.internal.database

import io.github.irgaly.kkvs.KkvsEnvironment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal expect class DatabaseConnection {
    /**
     * Exclusive Transaction
     */
    suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R

    /**
     * Exclusive Transaction
     */
    suspend fun transaction(body: () -> Unit)


    /**
     * Delete all records from all tables
     *
     * * this does not drop tables
     */
    suspend fun deleteAll()

    suspend fun getDatabaseStatus(): String
}

internal expect fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): DatabaseConnection
