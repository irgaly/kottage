package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.KottageEventFlow
import io.github.irgaly.kottage.internal.database.createDatabaseConnection
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventFlow
import io.github.irgaly.kottage.internal.repository.KottageRepositoryFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

internal class KottageDatabaseManager(
    fileName: String,
    directoryPath: String,
    private val environment: KottageEnvironment,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher + SupervisorJob())
) {
    private val databaseConnection by lazy {
        createDatabaseConnection(fileName, directoryPath, environment, dispatcher)
    }

    private val calendar get() = environment.calendar
    private val _eventFlow = ItemEventFlow(environment.calendar.nowUnixTimeMillis(), scope)

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

    val eventFlow: Flow<ItemEvent> = _eventFlow.flow

    suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R =
        databaseConnection.transactionWithResult(bodyWithReturn)

    suspend fun transaction(body: () -> Unit) = databaseConnection.transaction(body)
    suspend fun deleteAll() {
        databaseConnection.deleteAll()
    }

    suspend fun compact() {
        val statsRepository = statsRepository.await()
        val operator = operator.await()
        val now = calendar.nowUnixTimeMillis()
        databaseConnection.transaction {
            operator.evictCaches(now)
            operator.evictEvents(now)
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

    /**
     * Publish Events
     */
    suspend fun onEventCreated(eventId: String) {
        val operator = operator.await()
        val limit = 100L
        _eventFlow.updateWithLock { lastEvent, emit ->
            var lastEventTime = lastEvent.time
            var remains = true
            while (remains) {
                val events = databaseConnection.transactionWithResult {
                    operator.getEvents(
                        afterUnixTimeMillisAt = lastEventTime,
                        limit = limit
                    )
                }
                remains = (limit <= events.size)
                events.lastOrNull()?.let {
                    lastEventTime = it.createdAt
                }
                events.forEach {
                    emit(it)
                }
            }
        }
    }

    fun eventFlow(afterUnixTimeMillisAt: Long? = null, itemType: String? = null): KottageEventFlow {
        return KottageEventFlow(
            afterUnixTimeMillisAt,
            itemType,
            _eventFlow,
            databaseConnection,
            operator
        )
    }
}
