package io.github.irgaly.kottage

import androidx.test.platform.app.InstrumentationRegistry
import io.github.irgaly.kottage.platform.contextOf
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.common.KotestInternal
import io.kotest.core.spec.Spec
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.listener.CollectingTestEngineListener
import io.kotest.engine.test.TestResult
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.reflect.KClass

class AndroidTest {
    companion object {
        @BeforeAll
        fun setup() {
            KottageSpec.context = contextOf(InstrumentationRegistry.getInstrumentation().context)
        }
    }

    @Test
    fun kottageCacheTest() {
        executeTest(KottageCacheTest::class)
    }

    @Test
    fun kottageEventTest() {
        executeTest(KottageEventTest::class)
    }

    @Test
    fun kottageListTest() {
        executeTest(KottageListTest::class)
    }

    @Test
    fun kottageMigrationTest() {
        executeTest(KottageMigrationTest::class)
    }

    @Test
    fun kottageTest() {
        executeTest(KottageTest::class)
    }

    @OptIn(KotestInternal::class)
    private fun<T: Spec> executeTest(targetClass: KClass<T>) {
        val listener = CollectingTestEngineListener()
        TestEngineLauncher()
            .withListener(listener)
            .withClasses(targetClass).launch()
        listener.tests.map { entry ->
            {
                val testCase = entry.key
                val descriptor = testCase.descriptor.path().value
                val cause = when (val value = entry.value) {
                    is TestResult.Error -> value.cause
                    is TestResult.Failure -> value.cause
                    else -> null
                }
                assertFalse(entry.value.isErrorOrFailure) {
                    """$descriptor
                    |${cause?.stackTraceToString()}""".trimMargin()
                }
            }
        }.let {
            assertAll(it)
        }
        println("${targetClass.simpleName} Total ${listener.tests.size}, Failure ${listener.tests.count { it.value.isErrorOrFailure }}")
    }
}
