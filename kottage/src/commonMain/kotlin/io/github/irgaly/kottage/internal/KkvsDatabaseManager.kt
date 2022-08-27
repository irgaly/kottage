package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KkvsEnvironment
import io.github.irgaly.kottage.internal.database.createDatabaseConnection
import io.github.irgaly.kottage.internal.repository.KkvsItemRepository
import io.github.irgaly.kottage.internal.repository.KkvsRepositoryFactory
import io.github.irgaly.kottage.platform.Files
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class KkvsDatabaseManager(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val databaseConnection by lazy {
        if (!Files.exists(directoryPath)) {
            // TODO: async directory creation
            // FIX: mkdirs in createDatabaseConnection()
            Files.mkdirs(directoryPath)
        }
        createDatabaseConnection(fileName, directoryPath, environment, dispatcher)
    }

    private val repositoryFactory by lazy {
        KkvsRepositoryFactory(databaseConnection)
    }

    fun getItemRepository(itemType: String): KkvsItemRepository {
        return repositoryFactory.createItemRepository(itemType)
    }

    val itemEventRepository by lazy {
        repositoryFactory.createItemEventRepository()
    }

    suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R =
        databaseConnection.transactionWithResult(bodyWithReturn)

    suspend fun transaction(body: () -> Unit) = databaseConnection.transaction(body)
    suspend fun deleteAll() {
        databaseConnection.deleteAll()
    }

    suspend fun compact() {
        databaseConnection.compact()
    }

    suspend fun getDatabaseStatus(): String {
        return databaseConnection.getDatabaseStatus()
    }

    suspend fun backupTo(file: String, directoryPath: String) {
        databaseConnection.backupTo(file, directoryPath)
    }
}
