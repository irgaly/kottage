package io.github.irgaly.kottage.internal.database

internal interface DatabaseConnection {
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

