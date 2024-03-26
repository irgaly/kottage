package io.github.irgaly.kottage

import io.github.irgaly.kottage.strategy.KottageFifoStrategy
import io.github.irgaly.kottage.strategy.KottageLruStrategy
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import korlibs.time.DateTime
import korlibs.time.days
import korlibs.time.milliseconds

class KottageCacheTest : KottageSpec("kottage_cache", body = {
    describe("Kottage Cache Test") {
        context("Cache Expire") {
            val (kottage, calendar) = kottage()
            it("defaultExpireTime 経過で cache が消えること") {
                val cache = kottage.cache("defaultExpireTime") {
                    defaultExpireTime = 1.days
                }
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
                val cache = kottage.cache("put_expireTime") {
                    defaultExpireTime = 2.days
                }
                cache.put("expire1", "value", 1.days)
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
            val (compactionKottage, calendar) = kottage("compaction") {
                autoCompactionDuration = 1.days
            }
            val cache = compactionKottage.cache("cache") {
                defaultExpireTime = 2.days
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
            val (kottage, calendar) = kottage()
            val cache = kottage.cache("fifo") {
                strategy = KottageFifoStrategy(5, 3)
            }
            it("maxEntryCount を超えたら reduceCount だけ削除される") {
                cache.put("1", "")
                // created_at 重複のテスト
                cache.put("1_2", "")
                calendar.now += 1.milliseconds
                cache.put("2", "")
                calendar.now += 1.milliseconds
                cache.put("3", "")
                calendar.now += 1.milliseconds
                cache.put("4", "")
                calendar.now += 1.milliseconds
                cache.put("5", "")
                cache.exists("1") shouldBe false
                cache.exists("1_2") shouldBe false
                cache.exists("2") shouldBe false
                cache.exists("3") shouldBe true
                cache.exists("4") shouldBe true
                cache.exists("5") shouldBe true
            }
        }
        context("LRU Strategy") {
            val (kottage, calendar) = kottage()
            val cache = kottage.cache("lru") {
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
                itemExpireTime = 30.days
            }
            it("List に追加されたアイテムは expire されない") {
                cache.put("key1", "value1", 1.days)
                cache.put("key2", "value2", 1.days)
                list.addKey("key1")
                calendar.now += 1.days
                cache.get<String>("key1") shouldBe "value1"
                cache.getOrNull<String>("key2") shouldBe null
                calendar.now += 29.days
                cache.getOrNull<String>("Key1") shouldBe null
            }
        }
        context("Strategy + List") {
            val (kottage, calendar) = kottage()
            val cache = kottage.cache("strategy_list") {
                strategy = KottageFifoStrategy(3, 2)
            }
            val list = cache.list("list_strategy_list")
            it("List にデータがあれば maxEntryCount を超えても削除されない") {
                list.add("1", "")
                calendar.now += 1.milliseconds
                cache.put("2", "")
                calendar.now += 1.milliseconds
                list.add("3", "")
                calendar.now += 1.milliseconds
                list.add("4", "")
                calendar.now += 1.milliseconds
                cache.put("5", "")
                cache.exists("1") shouldBe true
                cache.exists("2") shouldBe false
                cache.exists("3") shouldBe true
                cache.exists("4") shouldBe true
                cache.exists("5") shouldBe false
            }
        }
    }
})
