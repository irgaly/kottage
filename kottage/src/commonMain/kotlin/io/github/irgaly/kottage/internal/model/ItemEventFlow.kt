package io.github.irgaly.kottage.internal.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ItemEventFlow(initialTime: Long, scope: CoroutineScope) {
    val flow: SharedFlow<ItemEvent>

    private val mutex = Mutex()
    private val source = MutableSharedFlow<ItemEvent>(
        // イベント送信漏れが発生しないように SUSPEND
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private var lastEventTime: Long = initialTime

    init {
        flow = source.asSharedFlow()
    }

    suspend fun updateWithLock(block: suspend (latestEventTime: Long, emit: suspend (event: ItemEvent) -> Unit) -> Long) {
        mutex.withLock {
            lastEventTime = block(lastEventTime, source::emit)
        }
    }

    suspend fun withLock(block: suspend (latestEventTime: Long) -> Unit) {
        mutex.withLock {
            block(lastEventTime)
        }
    }
}
