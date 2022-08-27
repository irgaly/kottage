package io.github.irgaly.kkvs.extension

import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
inline fun CloseableCoroutineDispatcher.use(block: (CloseableCoroutineDispatcher) -> Unit) {
    try {
        block(this)
    } finally {
        close()
    }
}
