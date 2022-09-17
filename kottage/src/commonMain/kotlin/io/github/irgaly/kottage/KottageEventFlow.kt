package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.KottageOperator
import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.model.ItemEventFlow
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.*

/**
 * Resumeable Event Flow
 */
class KottageEventFlow internal constructor(
    initialTime: Long?,
    itemType: String?,
    private val eventFlow: ItemEventFlow,
    private val databaseConnection: DatabaseConnection,
    private val operator: Deferred<KottageOperator>
): Flow<KottageEvent> {
    private var lastEventTime: Long? = initialTime

    private val flow: Flow<KottageEvent> = flow {
        eventFlow.withLock { _ ->
            lastEventTime?.let {
                // lastEventTime が設定されていれば、前回からの差分を流す
                val operator = operator.await()
                val limit = 100L
                var lastEventTime = it
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
            emitAll(
                eventFlow.flow.let { flow ->
                        if (itemType != null) {
                            flow.filter {
                                it.itemType == itemType
                            }
                        } else flow
                    }.onEach {
                        this@KottageEventFlow.lastEventTime = it.createdAt
                    }.map { KottageEvent.from(it) }
            )
        }
    }

    override suspend fun collect(collector: FlowCollector<KottageEvent>) {
        flow.collect(collector)
    }
}
