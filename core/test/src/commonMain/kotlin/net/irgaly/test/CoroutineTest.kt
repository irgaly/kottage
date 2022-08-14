package net.irgaly.test

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect fun runBlockingTest(context: CoroutineContext = EmptyCoroutineContext, block: suspend TestCoroutineScope.() -> Unit)
expect interface TestCoroutineScope
