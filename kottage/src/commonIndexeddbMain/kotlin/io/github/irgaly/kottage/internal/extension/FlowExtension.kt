package io.github.irgaly.kottage.internal.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

internal fun <T> Flow<T>.take(count: Long): Flow<T> {
    var consumed = 0L
    return takeWhile { consumed < count }
        .onEach { consumed++ }
}
