package io.github.irgaly.kkvs

import com.soywiz.klock.DateTime
import io.github.irgaly.kkvs.platform.Context
import io.github.irgaly.kkvs.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KkvsTest: DescribeSpec({
    val tempDirectory = tempdir()
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
    println("tempDirectory = $tempDirectory")
    describe("Kkvs") {
        context("storage モード") {
            val kkvs = Kkvs(
                "test",
                tempDirectory,
                KkvsEnvironment(Context(), calendar)
            )
            val storage = kkvs.storage(
                "storage1",
                kkvsStorage {
                }
            )
            println(kkvs.getDatabaseStatus())
            it("put, get で値を保持できている") {
                storage.put("key", "test")
                val value: String = storage.get("key")
                value shouldBe "test"
            }
        }
        context("独立 Kkvs インスタンス") {
            it("並列書き込み: 100") {
                repeat(100) { id ->
                    launch(Dispatchers.Default) {
                        val kkvs = Kkvs(
                            "test",
                            tempDirectory,
                            KkvsEnvironment(Context(), calendar)
                        )
                        val storage = kkvs.storage(
                            "storage1",
                            kkvsStorage {
                            }
                        )
                        storage.put("key$id", "value$id")
                        val value = storage.get<String>("key$id")
                        value shouldBe "value$id"
                    }
                }
            }
        }
    }
})
