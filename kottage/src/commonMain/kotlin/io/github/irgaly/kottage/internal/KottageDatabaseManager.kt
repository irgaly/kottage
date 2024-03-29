package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.KottageEventFlow
import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventFlow
import io.github.irgaly.kottage.internal.platform.PlatformFactory
import io.github.irgaly.kottage.platform.KottageSystemCalendar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow

internal class KottageDatabaseManager(
    fileName: String,
    directoryPath: String,
    private val options: KottageOptions,
    private val environment: KottageEnvironment,
    scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineScope by scope {
    private val databaseConnection by lazy {
        PlatformFactory().createDatabaseConnectionFactory()
            .createDatabaseConnection(fileName, directoryPath, environment, scope, dispatcher)
    }

    private val calendar = (environment.calendar ?: KottageSystemCalendar())
    private val _eventFlow = ItemEventFlow(calendar.nowUnixTimeMillis(), scope)

    private val repositoryFactory by lazy {
        PlatformFactory().createKottageRepositoryFactory(databaseConnection)
    }

    val databaseConnectionClosed: Boolean get() = databaseConnection.closed

    val itemRepository = async(dispatcher, CoroutineStart.LAZY) {
        repositoryFactory.createItemRepository()
    }

    val itemListRepository = async(dispatcher, CoroutineStart.LAZY) {
        repositoryFactory.createItemListRepository()
    }

    val itemEventRepository = async(dispatcher, CoroutineStart.LAZY) {
        repositoryFactory.createItemEventRepository()
    }

    val statsRepository = async(dispatcher, CoroutineStart.LAZY) {
        repositoryFactory.createStatsRepository()
    }

    val operator = async(dispatcher, CoroutineStart.LAZY) {
        KottageOperator(
            options,
            itemRepository.await(),
            itemListRepository.await(),
            itemEventRepository.await(),
            statsRepository.await()
        )
    }

    val eventFlow: Flow<ItemEvent> = _eventFlow.flow

    suspend fun getStorageOperator(storage: KottageStorage): KottageStorageOperator {
        return KottageStorageOperator(
            itemType = storage.name,
            storage.options,
            operator.await(),
            itemRepository.await(),
            itemListRepository.await(),
            itemEventRepository.await(),
            statsRepository.await()
        )
    }

    suspend fun getListOperator(
        kottageList: KottageList,
        storage: KottageStorage
    ): KottageListOperator {
        return KottageListOperator(
            itemType = storage.name,
            listType = kottageList.name,
            kottageList.options,
            storage.options,
            operator.await(),
            getStorageOperator(storage),
            itemRepository.await(),
            itemListRepository.await(),
            itemEventRepository.await(),
            statsRepository.await()
        )
    }

    suspend fun <R> transactionWithResult(bodyWithReturn: suspend Transaction.() -> R): R =
        databaseConnection.transactionWithResult(bodyWithReturn)

    suspend fun transaction(body: suspend Transaction.() -> Unit) = databaseConnection.transaction(body)
    suspend fun deleteAll() {
        databaseConnection.deleteAll()
    }

    suspend fun compact(force: Boolean = false) {
        val operator = operator.await()
        val now = calendar.nowUnixTimeMillis()
        val beforeExpireAt = options.garbageCollectionTimeOfInvalidatedListEntries?.let {
            now - it.inWholeMilliseconds
        }
        val compactionRequired = databaseConnection.transactionWithResult {
            val required = (force || operator.getAutoCompactionNeeded(this, now))
            if (required) {
                if (beforeExpireAt != null) {
                    // List Entry の自動削除が有効
                    operator.evictExpiredListEntries(
                        this,
                        now = now,
                        beforeExpireAt = beforeExpireAt
                    )
                } else {
                    // List Entry Invalidate のみ
                    operator.invalidateExpiredListEntries(this, now = now)
                }
                operator.evictCaches(this, now)
                operator.evictEvents(this, now)
                operator.evictEmptyStats(this)
                operator.updateLastEvictAt(this, now)
            }
            required
        }
        if (compactionRequired) {
            databaseConnection.compact()
        }
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
    suspend fun onEventCreated() {
        val operator = operator.await()
        val limit = 100L
        _eventFlow.updateWithLock { latestEventTime, emit ->
            var lastEventTime = latestEventTime
            var remains = true
            while (remains) {
                val events = databaseConnection.transactionWithResult {
                    operator.getEvents(
                        this,
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
            lastEventTime
        }
    }

    fun eventFlow(afterUnixTimeMillisAt: Long? = null): KottageEventFlow {
        return KottageEventFlow(
            afterUnixTimeMillisAt,
            KottageEventFlow.EventFlowType.All,
            _eventFlow,
            databaseConnection,
            operator,
            dispatcher
        )
    }

    fun itemEventFlow(itemType: String, afterUnixTimeMillisAt: Long? = null): KottageEventFlow {
        return KottageEventFlow(
            afterUnixTimeMillisAt,
            KottageEventFlow.EventFlowType.Item(itemType = itemType),
            _eventFlow,
            databaseConnection,
            operator,
            dispatcher
        )
    }

    fun listEventFlow(listType: String, afterUnixTimeMillisAt: Long? = null): KottageEventFlow {
        return KottageEventFlow(
            afterUnixTimeMillisAt,
            KottageEventFlow.EventFlowType.List(listType = listType),
            _eventFlow,
            databaseConnection,
            operator,
            dispatcher
        )
    }

    suspend fun closeDatabaseConnection() {
        databaseConnection.close()
    }
}
