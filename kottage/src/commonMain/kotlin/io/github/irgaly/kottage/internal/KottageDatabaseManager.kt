package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.internal.database.createDatabaseConnection
import io.github.irgaly.kottage.internal.repository.KottageRepositoryFactory
import io.github.irgaly.kottage.platform.Files
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class KottageDatabaseManager(
    fileName: String,
    directoryPath: String,
    private val environment: KottageEnvironment,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val databaseConnection by lazy {
        if (!Files.exists(directoryPath)) {
            // TODO: async directory creation
            // FIX: mkdirs in createDatabaseConnection()
            Files.mkdirs(directoryPath)
        }
        createDatabaseConnection(fileName, directoryPath, environment, dispatcher)
    }

    private val calendar get() = environment.calendar

    private val repositoryFactory by lazy {
        KottageRepositoryFactory(databaseConnection)
    }

    private val operator by lazy {
        KottageOperator(null, databaseConnection)
    }

    val itemRepository by lazy {
        repositoryFactory.createItemRepository()
    }

    val itemEventRepository by lazy {
        repositoryFactory.createItemEventRepository()
    }

    fun createOperator(itemType: String): KottageOperator {
        return KottageOperator(itemType, databaseConnection)
    }

    suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R =
        databaseConnection.transactionWithResult(bodyWithReturn)

    suspend fun transaction(body: () -> Unit) = databaseConnection.transaction(body)
    suspend fun deleteAll() {
        databaseConnection.deleteAll()
    }

    suspend fun compact() {
        val now = calendar.nowUtcEpochTimeMillis()
        databaseConnection.transaction {
            operator.compactAllType(now)
        }
        databaseConnection.compact()
    }

    suspend fun getDatabaseStatus(): String {
        return databaseConnection.getDatabaseStatus()
    }

    suspend fun backupTo(file: String, directoryPath: String) {
        databaseConnection.backupTo(file, directoryPath)
    }
}
