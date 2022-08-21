package io.github.irgaly.kkvs

import com.soywiz.klock.DateTime
import io.github.irgaly.kkvs.platform.Context
import io.github.irgaly.kkvs.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

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
    }
})
