package io.github.irgaly.kottage.internal.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ItemEventFlow(initialTime: Long, scope: CoroutineScope) {
    val flow: SharedFlow<ItemEvent>

    private val mutex = Mutex()
    private val source = MutableSharedFlow<ItemEvent>(
        // イベント送信漏れが発生しないように SUSPEND
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val lastEvent = source.map {
        Event(it.createdAt, it)
    }.stateIn(scope, SharingStarted.Eagerly, Event(initialTime, null))

    init {
        flow = source.asSharedFlow()
    }

    suspend fun updateWithLock(block: suspend (latestEvent: Event, emit: suspend (event: ItemEvent) -> Unit) -> Unit) {
        mutex.withLock {
            block(lastEvent.value, source::emit)
        }
    }

    data class Event(
        val time: Long,
        val event: ItemEvent?
    )
}
