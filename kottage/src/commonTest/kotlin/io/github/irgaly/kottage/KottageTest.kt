package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import io.github.irgaly.kottage.platform.Context
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KottageTest : DescribeSpec({
    val tempDirectory = tempdir()
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
    println("tempDirectory = $tempDirectory")
    describe("Kottage") {
        context("storage モード") {
            val kottage = Kottage(
                "test",
                tempDirectory,
                KottageEnvironment(Context(), calendar)
            )
            val storage = kottage.storage("storage1")
            it("put, get で値を保持できている") {
                storage.put("key", "test")
                val value: String = storage.get("key")
                value shouldBe "test"
            }
        }
        context("独立 Kottage インスタンス") {
            it("並列書き込み: 100") {
                repeat(100) { id ->
                    launch(Dispatchers.Default) {
                        val kottage = Kottage(
                            "test",
                            tempDirectory,
                            KottageEnvironment(Context(), calendar)
                        )
                        val storage = kottage.storage("storage1")
                        storage.put("key$id", "value$id")
                        val value = storage.get<String>("key$id")
                        value shouldBe "value$id"
                    }
                }
            }
        }
    }
})
