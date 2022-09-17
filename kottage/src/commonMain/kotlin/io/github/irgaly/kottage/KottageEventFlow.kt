package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.KottageOperator
import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.model.ItemEventFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex

/**
 * Resumeable Event Flow
 */
class KottageEventFlow internal constructor(
    initialTime: Long?,
    itemType: String?,
    private val eventFlow: ItemEventFlow,
    private val databaseConnection: DatabaseConnection,
    private val operator: Deferred<KottageOperator>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): Flow<KottageEvent> {
    private var lastEventTime: Long? = initialTime
    private val subscribing = Mutex()

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
                            operator.getEvents(
                                afterUnixTimeMillisAt = lastEventTime,
                                itemType = itemType,
                                limit = limit
                            )
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
                CoroutineScope(currentCoroutineContext()).launch(start = CoroutineStart.UNDISPATCHED) {
                    // collect を開始した状態で eventFlow.withLock を抜ける
                    eventFlow.flow.collect {
                        bridgeFlow.emit(KottageEvent.from(it))
                    }
                }
            }
        })
    }.flattenConcat()
        .onCompletion {
            subscribing.unlock()
        }.flowOn(dispatcher)

    override suspend fun collect(collector: FlowCollector<KottageEvent>) {
        flow.collect(collector)
    }
}
