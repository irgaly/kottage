package io.github.irgaly.kottage

import androidx.test.platform.app.InstrumentationRegistry
import io.github.irgaly.kottage.platform.contextOf
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.common.KotestInternal
import io.kotest.core.spec.Spec
import io.kotest.core.spec.SpecRef
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
    suspend fun kottageCacheTest() {
        executeTest(KottageCacheTest::class)
    }

    @Test
    suspend fun kottageEventTest() {
        executeTest(KottageEventTest::class)
    }

    @Test
    suspend fun kottageListTest() {
        executeTest(KottageListTest::class)
    }

    @Test
    suspend fun kottageMigrationTest() {
        executeTest(KottageMigrationTest::class)
    }

    @Test
    suspend fun kottageTest() {
        executeTest(KottageTest::class)
    }

    @OptIn(KotestInternal::class)
    private suspend fun <T : Spec> executeTest(targetClass: KClass<T>) {
        val listener = CollectingTestEngineListener()
        TestEngineLauncher()
            .withListener(listener)
            .withSpecRefs(SpecRef.Reference(targetClass))
            .execute()
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
