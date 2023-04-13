package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.KottageOperator
import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.model.ItemEventFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.coroutineContext

/**
 * Resumable Event Flow
 */
class KottageEventFlow internal constructor(
    initialTime: Long?,
    eventFlowType: EventFlowType,
    private val eventFlow: ItemEventFlow,
    private val databaseConnection: DatabaseConnection,
    private val operator: Deferred<KottageOperator>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): Flow<KottageEvent> {
    private var lastEventTime: Long? = initialTime
    private val subscribing = Mutex()
    private val initializing = Mutex(locked = true)

    @OptIn(FlowPreview::class)
    private val flow: Flow<KottageEvent> = flow {
        // cold flow
        if (!subscribing.tryLock()) {
            throw IllegalStateException("KottageEventFlow cannot be observed from multiple observer.")
        }
        val bridgeFlow = MutableSharedFlow<KottageEvent>()
        emit(bridgeFlow.onSubscription {
            eventFlow.withLock { _ ->
                lastEventTime?.let { time ->
                    // lastEventTime が設定されていれば、前回からの差分を流す
                    val operator = operator.await()
                    val limit = 100L
                    var lastEventTime = time
                    var remains = true
                    while (remains) {
                        val events = databaseConnection.transactionWithResult {
                            when (eventFlowType) {
                                EventFlowType.All -> {
                                    operator.getEvents(
                                        this@transactionWithResult,
                                        afterUnixTimeMillisAt = lastEventTime,
                                        limit = limit
                                    )
                                }

                                is EventFlowType.Item -> {
                                    operator.getItemEvents(
                                        this@transactionWithResult,
                                        itemType = eventFlowType.itemType,
                                        afterUnixTimeMillisAt = lastEventTime,
                                        limit = limit
                                    )
                                }

                                is EventFlowType.List -> {
                                    operator.getListEvents(
                                        this@transactionWithResult,
                                        listType = eventFlowType.listType,
                                        afterUnixTimeMillisAt = lastEventTime,
                                        limit = limit
                                    )
                                }
                            }
                        }
                        remains = (limit <= events.size)
                        events.lastOrNull()?.let {
                            lastEventTime = it.createdAt
                        }
                        events.forEach {
                            emit(KottageEvent.from(it))
                        }
                    }
                }
                checkNotNull(coroutineContext[Job]) { "KottageEventFlow: subscriber CoroutineScope should have Job" }
                CoroutineScope(coroutineContext).launch(start = CoroutineStart.UNDISPATCHED) {
                    // collect を開始した状態で eventFlow.withLock を抜ける
                    eventFlow.flow.let { flow ->
                        when (eventFlowType) {
                            EventFlowType.All -> {
                                flow
                            }

                            is EventFlowType.Item -> {
                                flow.filter {
                                    (it.itemListType == null) && (it.itemType == eventFlowType.itemType)
                                }
                            }

                            is EventFlowType.List -> {
                                flow.filter {
                                    (it.itemListType == eventFlowType.listType)
                                }
                            }
                        }
                    }.collect {
                        bridgeFlow.emit(KottageEvent.from(it))
                    }
                }
                initializing.unlock()
            }
        })
    }.flattenConcat()
        .onCompletion {
            subscribing.unlock()
        }.flowOn(dispatcher)

    override suspend fun collect(collector: FlowCollector<KottageEvent>) {
        flow.collect(collector)
    }

    /**
     * ensure and wait until subscription starts.
     *
     * This method is mainly for debug purpose.
     */
    suspend fun ensureSubscribed() {
        if (initializing.isLocked) {
            initializing.lock()
            initializing.unlock()
        }
    }

    internal sealed interface EventFlowType {
        object All : EventFlowType
        data class Item(
            val itemType: String
        ) : EventFlowType

        data class List(
            val listType: String
        ) : EventFlowType
    }
}
