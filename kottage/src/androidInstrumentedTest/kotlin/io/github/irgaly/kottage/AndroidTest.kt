package io.github.irgaly.kottage

import androidx.test.platform.app.InstrumentationRegistry
import io.github.irgaly.kottage.platform.contextOf
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.common.KotestInternal
import io.kotest.core.spec.Spec
import io.kotest.core.spec.SpecRef
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.listener.CollectingTestEngineListener
import io.kotest.engine.test.TestResult
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.BeforeClass
import org.junit.Test
import kotlin.reflect.KClass

class AndroidTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            KottageSpec._context = contextOf(InstrumentationRegistry.getInstrumentation().context)
        }
    }

    @Test
    fun kottageCacheTest() = runTest {
        executeTest(KottageCacheTest::class)
    }

    @Test
    fun kottageEventTest() = runTest {
        executeTest(KottageEventTest::class)
    }

    @Test
    fun kottageListTest() = runTest {
        executeTest(KottageListTest::class)
    }

    @Test
    fun kottageMigrationTest() = runTest {
        executeTest(KottageMigrationTest::class)
    }

    @Test
    fun kottageTest() = runTest {
        executeTest(KottageTest::class)
    }

    @OptIn(KotestInternal::class)
    private suspend fun <T : Spec> executeTest(targetClass: KClass<T>) {
        val listener = CollectingTestEngineListener()
        TestEngineLauncher()
            .withListener(listener)
            .withSpecRefs(SpecRef.Reference(targetClass))
            .execute()
        assertSoftly {
            for (entry in listener.tests) {
                val testCase = entry.key
                val descriptor = testCase.descriptor.path().value
                val cause = when (val value = entry.value) {
                    is TestResult.Error -> value.cause
                    is TestResult.Failure -> value.cause
                    else -> null
                }
                withClue({
                    """$descriptor
                    |${cause?.stackTraceToString()}""".trimMargin()
                }) {
                    entry.value.isErrorOrFailure shouldBe false
                }
            }
        }
        println("${targetClass.simpleName} Total ${listener.tests.size}, Failure ${listener.tests.count { it.value.isErrorOrFailure }}")
    }
}
