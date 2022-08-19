package io.github.irgaly.kkvs.internal.database

import io.github.irgaly.kkvs.KkvsEnvironment

internal expect class DatabaseConnection {
    suspend fun <R> transactionWithResult(bodyWithReturn: suspend () -> R): R
    suspend fun transaction(body: suspend () -> Unit)

    /**
     * Delete all records from all tables
     *
     * * this does not drop tables
     */
    suspend fun deleteAll()
}

internal expect fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
): DatabaseConnection
