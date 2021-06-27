package net.irgaly.test

import org.spekframework.spek2.dsl.Skip
import org.spekframework.spek2.dsl.TestBody
import org.spekframework.spek2.style.specification.Suite
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Coroutine に対応した it
 */
fun Suite.itBlocking(description: String, skip: Skip = Skip.No, timeout: Long = delegate.defaultTimeout, context: CoroutineContext = EmptyCoroutineContext, body: suspend TestBody.() -> Unit) {
    it(description, skip, timeout) {
        runBlockingTest(context) {
            body.invoke(this@it)
        }
    }
}

/**
 * Coroutine に対応した xit
 */
fun Suite.xitBlocking(description: String, reason: String = "", timeout: Long = delegate.defaultTimeout, context: CoroutineContext = EmptyCoroutineContext, body: suspend TestBody.() -> Unit) {
    xit(description, reason, timeout) {
        runBlockingTest(context) {
            body.invoke(this@xit)
        }
    }
}

/**
 * Coroutine に対応した before
 */
fun Suite.beforeBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestCoroutineScope.() -> Unit
) {
    beforeGroup {
        runBlockingTest(context, testBody)
    }
}

/**
 * Coroutine に対応した after
 */
fun Suite.afterBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestCoroutineScope.() -> Unit
) {
    afterGroup {
        runBlockingTest(context, testBody)
    }
}

/**
 * Coroutine に対応した beforeEach
 */
fun Suite.beforeEachBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestCoroutineScope.() -> Unit
) {
    beforeEachTest {
        runBlockingTest(context, testBody)
    }
}

/**
 * Coroutine に対応した afterEach
 */
fun Suite.afterEachBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestCoroutineScope.() -> Unit
) {
    afterEachTest {
        runBlockingTest(context, testBody)
    }
}
