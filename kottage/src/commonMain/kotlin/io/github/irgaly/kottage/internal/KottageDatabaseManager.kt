package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.internal.database.createDatabaseConnection
import io.github.irgaly.kottage.internal.repository.KottageRepositoryFactory
import kotlinx.coroutines.*

internal class KottageDatabaseManager(
    fileName: String,
    directoryPath: String,
    private val environment: KottageEnvironment,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val databaseConnection by lazy {
        createDatabaseConnection(fileName, directoryPath, environment, dispatcher)
    }

    private val calendar get() = environment.calendar

    private val repositoryFactory by lazy {
        KottageRepositoryFactory(databaseConnection)
    }

    @OptIn(DelicateCoroutinesApi::class)
    val itemRepository = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        repositoryFactory.createItemRepository()
    }

    @OptIn(DelicateCoroutinesApi::class)
    val itemEventRepository = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        repositoryFactory.createItemEventRepository()
    }

    @OptIn(DelicateCoroutinesApi::class)
    val statsRepository = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        repositoryFactory.createStatsRepository()
    }

    @OptIn(DelicateCoroutinesApi::class)
    val operator = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        KottageOperator(
            itemRepository.await(),
            itemEventRepository.await(),
            statsRepository.await()
        )
    }

    suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R =
        databaseConnection.transactionWithResult(bodyWithReturn)

    suspend fun transaction(body: () -> Unit) = databaseConnection.transaction(body)
    suspend fun deleteAll() {
        databaseConnection.deleteAll()
    }

    suspend fun compact() {
        val statsRepository = statsRepository.await()
        val operator = operator.await()
        val now = calendar.nowUtcEpochTimeMillis()
        databaseConnection.transaction {
            operator.evictCache(now)
            statsRepository.updateLastEvictAt(now)
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
