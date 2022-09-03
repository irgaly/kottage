package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import io.github.irgaly.kottage.platform.Context
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.duration
import io.github.irgaly.test.extension.tempdir
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class KottageCacheTest : DescribeSpec({
    val tempDirectory = tempdir()
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
    describe("Kottage Cache Test") {
        val kottage = Kottage(
            "test",
            tempDirectory,
            KottageEnvironment(Context(), calendar)
        )
        context("debug 機能") {
            it("tempDirectory 表示") {
                println("tempDirectory = $tempDirectory")
            }
        }
        context("Cache Expire") {
            val cache = kottage.cache("cache1") {
                defaultExpireTime = 1.days.duration
            }
            it("defaultExpireTime 経過で cache が消えること") {
                cache.put("expire1", "value")
                calendar.now += 1.days
                cache.exists("expire1") shouldBe false
                cache.getOrNull<String>("expire1") shouldBe null
                cache.getEntryOrNull<String>("expire1") shouldBe null
                shouldThrow<NoSuchElementException> {
                    cache.get<String>("expire1")
                }
                shouldThrow<NoSuchElementException> {
                    cache.getEntry<String>("expire1")
                }
            }
        }
    }
})
