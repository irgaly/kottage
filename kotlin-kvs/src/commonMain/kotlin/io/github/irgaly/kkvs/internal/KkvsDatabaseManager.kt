package io.github.irgaly.kkvs.internal

import io.github.irgaly.kkvs.KkvsEnvironment
import io.github.irgaly.kkvs.internal.database.createDatabaseConnection
import io.github.irgaly.kkvs.internal.repository.KkvsItemRepository
import io.github.irgaly.kkvs.internal.repository.KkvsRepositoryFactory

internal class KkvsDatabaseManager(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    private val databaseConnection by lazy {
        createDatabaseConnection(fileName, directoryPath, environment)
    }

    val repositoryFactory by lazy {
        KkvsRepositoryFactory(databaseConnection)
    }

    fun getItemRepository(itemType: String): KkvsItemRepository {
        return repositoryFactory.createItemRepository(itemType)
    }

    val itemEventRepository by lazy {
        repositoryFactory.createItemEventRepository()
    }

    suspend fun <R> transactionWithResult(bodyWithReturn: suspend () -> R): R
        = databaseConnection.transactionWithResult(bodyWithReturn)
    suspend fun transaction(body: suspend () -> Unit)
        = databaseConnection.transaction(body)
    suspend fun deleteAll() {
        databaseConnection.deleteAll()
    }
}
