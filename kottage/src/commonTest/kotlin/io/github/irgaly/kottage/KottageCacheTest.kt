package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.milliseconds
import io.github.irgaly.kottage.extension.buildKottage
import io.github.irgaly.kottage.platform.KottageContext
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
    fun kottage(
        name: String = "kottage_cache", builder: (KottageOptions.Builder.() -> Unit)? = null
    ): Pair<Kottage, TestCalendar> = buildKottage(name, tempDirectory, builder)
    describe("Kottage Cache Test") {
        val kottage = Kottage(
            "test",
            tempDirectory,
            KottageEnvironment(KottageContext(), calendar)
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
            it("put expireTime 経過で cache が消えること") {
                val cache = kottage.cache("cache2") {
                    defaultExpireTime = 2.days.duration
                }
                cache.put("expire1", "value", 1.days.duration)
                cache.put("expire2", "value")
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
                cache.exists("expire2") shouldBe true
            }
        }
        context("Auto Compaction") {
            val compactionKottage = Kottage(
                "compaction",
                tempDirectory,
                KottageEnvironment(KottageContext(), calendar)
            ) {
                autoCompactionDuration = 1.days.duration
            }
            val cache = compactionKottage.cache("cache1") {
                defaultExpireTime = 2.days.duration
            }
            it("autoCompactionDuration 経過で cache が自動削除される") {
                calendar.setUtc(DateTime(2022, 1, 1))
                cache.put("a", "") // this triggers initial compaction
                calendar.now += 1.milliseconds
                cache.put("b", "")
                calendar.setUtc(DateTime(2022, 1, 2))
                cache.put("c", "") // this triggers first compaction
                calendar.setUtc(DateTime(2022, 1, 3))
                cache.put("d", "") // this triggers second compaction
                // reset time for existing check
                calendar.setUtc(DateTime(2022, 1, 1))
                cache.exists("a") shouldBe false
                cache.exists("b") shouldBe true
                cache.exists("c") shouldBe true
                cache.exists("d") shouldBe true
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
        context("List") {
            val (kottage, calendar) = kottage()
            val cache = kottage.cache("list_expire")
            val list = cache.list("list_list_expire") {
                itemExpireTime = 30.days.duration
            }
            it("List に追加されたアイテムは expire されない") {
                cache.put("key1", "value1", 1.days.duration)
                cache.put("key2", "value2", 1.days.duration)
                list.addKey("key1")
                calendar.now += 1.days
                cache.get<String>("key1") shouldBe "value1"
                cache.getOrNull<String>("key2") shouldBe null
                calendar.now += 29.days
                cache.getOrNull<String>("Key1") shouldBe null
            }
        }
    }
})
