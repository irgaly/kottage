package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.milliseconds
import io.github.irgaly.kottage.platform.Context
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.kottage.strategy.KottageFifoStrategy
import io.github.irgaly.kottage.strategy.KottageLruStrategy
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
        context("FIFO Strategy") {
            val cache = kottage.cache("cache2") {
                strategy = KottageFifoStrategy(4, 2)
            }
            it("maxEntryCount を超えたら reduceCount だけ削除される") {
                cache.put("1", "")
                calendar.now += 1.milliseconds
                cache.put("2", "")
                calendar.now += 1.milliseconds
                cache.put("3", "")
                calendar.now += 1.milliseconds
                cache.put("4", "")
                calendar.now += 1.milliseconds
                cache.put("5", "")
                cache.exists("1") shouldBe false
                cache.exists("2") shouldBe false
                cache.exists("3") shouldBe true
                cache.exists("4") shouldBe true
                cache.exists("5") shouldBe true
            }
        }
        context("LRU Strategy") {
            val cache = kottage.cache("cache3") {
                strategy = KottageLruStrategy(4, 2)
            }
            it("maxEntryCount を超えたら reduceCount だけ削除される") {
                cache.put("1", "")
                calendar.now += 1.milliseconds
                cache.put("2", "")
                calendar.now += 1.milliseconds
                cache.put("3", "")
                calendar.now += 1.milliseconds
                cache.put("4", "")
                calendar.now += 1.milliseconds
                // "2" へアクセス
                cache.get<String>("2")
                calendar.now += 1.milliseconds
                cache.put("5", "")
                cache.exists("1") shouldBe false
                cache.exists("2") shouldBe true
                cache.exists("3") shouldBe false
                cache.exists("4") shouldBe true
                cache.exists("5") shouldBe true
            }
        }
    }
})
