package io.github.irgaly.kottage

import com.soywiz.klock.days
import com.soywiz.klock.hours
import io.github.irgaly.kottage.extension.buildKottage
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.duration
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class KottageListTest : DescribeSpec({
    val tempDirectory = tempdir()
    val printListStatus = false
    fun kottage(
        name: String = "kottage_list", builder: (KottageOptions.Builder.() -> Unit)? = null
    ): Pair<Kottage, TestCalendar> = buildKottage(name, tempDirectory, builder)
    describe("Kottage List Test") {
        context("debug 機能") {
            it("tempDirectory 表示") {
                println("tempDirectory = $tempDirectory")
            }
        }
        context("List 基本操作") {
            it("add, get") {
                val cache = kottage().first.cache("add_get")
                val list = cache.list("list_add_get")
                list.add("key1", "value1")
                list.add("key2", "value2")
                list.addAll(
                    listOf(
                        kottageListValue("key3", "value3"),
                        kottageListValue("key4", "value4")
                    )
                )
                list.addKeys(listOf("key1", "key2"))
                list.addFirst("key5", "value5")
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
                cache.get<String>("key3") shouldBe "value3"
                cache.get<String>("key4") shouldBe "value4"
                cache.get<String>("key5") shouldBe "value5"
                list.getFirst()?.value<String>() shouldBe "value5"
                list.getByIndex(1)?.value<String>() shouldBe "value1"
                list.getByIndex(2)?.value<String>() shouldBe "value2"
                list.getByIndex(3)?.value<String>() shouldBe "value3"
                list.getByIndex(4)?.value<String>() shouldBe "value4"
                list.getByIndex(5)?.value<String>() shouldBe "value1"
                list.getByIndex(6)?.value<String>() shouldBe "value2"
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }
            it("remove") {
                val cache = kottage().first.cache("remove")
                val list = cache.list("list_remove")
                list.add("key1", "value1")
                list.add("key2", "value2")
                list.remove(checkNotNull(list.getFirst()).positionId)
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
                list.getFirst()?.value<String>() shouldBe "value2"
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }
            it("MetaData の読み書き") {
                val cache = kottage().first.cache("metadata")
                val list = cache.list("list_metadata")
                list.add(
                    "key1", "value1", KottageListMetaData(
                        "info", "prev", "current", "next"
                    )
                )
                val entry = checkNotNull(list.getFirst())
                entry.info shouldBe "info"
                entry.previousKey shouldBe "prev"
                entry.currentKey shouldBe "current"
                entry.nextKey shouldBe "next"
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }
        }
        context("List expiration") {
            val (kottage, calendar) = kottage()
            val cache = kottage.cache("list_expiration")
            val list = cache.list("list_list_expiration") {
                itemExpireTime = 1.days.duration
            }
            it("先頭・末尾の有効期限切れ Entry にアクセスできないこと") {
                val entry1 = list.add("key1", "value1")
                val entry2 = list.add("key2", "value2")
                calendar.now += 1.hours
                val entry3 = list.add("key3", "value3")
                val entry4 = list.add("key4", "value4")
                calendar.now += 1.hours
                val entry5 = list.insertAfter(entry2.positionId, "key5", "value5")
                calendar.now += 22.hours
                list.get(entry1.positionId)?.itemKey shouldBe "key5"
                list.get(entry2.positionId)?.itemKey shouldBe "key5"
                list.get(entry3.positionId)?.itemKey shouldBe "key3"
                list.get(entry4.positionId)?.itemKey shouldBe "key4"
                calendar.now += 1.hours
                list.get(entry3.positionId) shouldBe null
                list.get(entry4.positionId) shouldBe null
                list.get(entry5.positionId)?.itemKey shouldBe "key5"
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }

            it("先頭・末尾ではない有効期限切れ Entry にアクセスできる") {
                val entry1 = list.add("key1", "value1")
                val entry2 = list.add("key2", "value2")
                val entry3 = list.add("key3", "value3")
                calendar.now += 1.hours
                val entry4 = list.insertAfter(entry1.positionId, "key4", "value4")
                val entry5 = list.insertAfter(entry2.positionId, "key5", "value5")
                calendar.now += 23.hours
                list.get(entry1.positionId)?.itemKey shouldBe "key4"
                list.get(entry2.positionId)?.itemKey shouldBe "key2"
                list.get(entry3.positionId) shouldBe null
                list.get(entry4.positionId)?.itemKey shouldBe "key4"
                list.get(entry5.positionId)?.itemKey shouldBe "key5"
                calendar.now += 1.hours
                list.get(entry1.positionId) shouldBe null
                list.get(entry4.positionId) shouldBe null
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }
        }
        context("List compaction") {
            val (kottage, calendar) = kottage()
            val cache = kottage.cache("list_compaction")
            val list = cache.list("list_list_compaction") {
                itemExpireTime = 1.days.duration
            }
            it("expire した entry が削除される") {
                list.add("key1", "value1")
                list.compact()
                list.getSize() shouldBe 1
                calendar.now += 1.days
                list.getSize() shouldBe 0
                list.getDebugStatus() shouldContain "total = 1"
                list.compact()
                list.getDebugStatus() shouldContain "total = 0"
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }
        }
    }
})
