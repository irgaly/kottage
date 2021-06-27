package net.irgaly.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
actual fun runBlockingTest(context: CoroutineContext, block: suspend TestCoroutineScope.() -> Unit) = kotlinx.coroutines.test.runBlockingTest(context, block)

@OptIn(ExperimentalCoroutinesApi::class)
actual typealias TestCoroutineScope = kotlinx.coroutines.test.TestCoroutineScope
