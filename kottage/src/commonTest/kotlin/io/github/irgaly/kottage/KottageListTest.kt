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
                val entry2 = list.add("key2", "value2")
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
                list.getByIndex(
                    2,
                    fromPositionId = entry2.positionId
                )?.value<String>() shouldBe "value4"
                list.getByIndex(
                    1,
                    direction = KottageListDirection.Backward
                )?.value<String>() shouldBe "value1"
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }
            it("update") {
                val cache = kottage().first.cache("update")
                val list = cache.list("list_update")
                list.add("key1", "value1")
                list.update(checkNotNull(list.getFirst()).positionId, "key2", "value2")
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
                list.getFirst()?.value<String>() shouldBe "value2"
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
            it("Page 読み込み") {
                val cache = kottage().first.cache("page")
                val list = cache.list("list_page")
                list.addAll(
                    listOf(
                        kottageListValue("key1", "value1"),
                        kottageListValue("key2", "value2"),
                        kottageListValue("key3", "value3"),
                        kottageListValue("keyX", "valueX"),
                        kottageListValue("key4", "value4"),
                        kottageListValue("key5", "value5"),
                        kottageListValue("key6", "value6")
                    )
                )
                list.remove(checkNotNull(list.getByIndex(3)).positionId)
                val page0 = list.getPageFrom(null, 1)
                val page1 = list.getPageFrom(checkNotNull(page0.nextPositionId), 2)
                var page2 = list.getPageFrom(checkNotNull(page1.nextPositionId), 3)
                page0.hasPrevious shouldBe false
                page0.items[0].itemKey shouldBe "key1"
                page1.items[0].itemKey shouldBe "key2"
                page2.items[0].itemKey shouldBe "key4"
                page2.hasNext shouldBe false
                list.add("key7", "value7")
                page2 = list.getPageFrom(checkNotNull(page1.nextPositionId), 5)
                page2.items[0].itemKey shouldBe "key4"
                page2.items.last().itemKey shouldBe "key7"
                page2.hasNext shouldBe false
            }
            it("Page 読み込み: Reverse") {
                val cache = kottage().first.cache("page_reverse")
                val list = cache.list("list_page_reverse")
                list.addAll(
                    listOf(
                        kottageListValue("key1", "value1"),
                        kottageListValue("key2", "value2"),
                        kottageListValue("key3", "value3"),
                        kottageListValue("key4", "value4"),
                        kottageListValue("key5", "value5"),
                        kottageListValue("key6", "value6")
                    )
                )
                val page = list.getPageFrom(
                    checkNotNull(list.getByIndex(4)).positionId,
                    2,
                    direction = KottageListDirection.Backward
                )
                val lastPage = list.getPageFrom(
                    null,
                    2,
                    direction = KottageListDirection.Backward
                )
                page.hasPrevious shouldBe true
                page.hasNext shouldBe true
                page.items[0].itemKey shouldBe "key4"
                lastPage.hasPrevious shouldBe true
                lastPage.hasNext shouldBe false
                lastPage.items[0].itemKey shouldBe "key5"
            }
            it("Page: hasPrevious, hasNext の要素有無判定") {
                val storage = kottage().first.storage("hasprevious_hasnext")
                val list = storage.list("list_hasprevious_hasnext")
                list.addAll(
                    listOf(
                        kottageListValue("key1", "value1"),
                        kottageListValue("key2", "value2"),
                        kottageListValue("key3", "value3"),
                        kottageListValue("key4", "value4")
                    )
                )
                list.remove(checkNotNull(list.getFirst()).positionId)
                list.remove(checkNotNull(list.getLast()).positionId)
                val entry2 = checkNotNull(list.getFirst())
                val page1 = list.getPageFrom(entry2.positionId, 1)
                val page2 = list.getPageFrom(page1.nextPositionId, 1)
                page1.hasPrevious shouldBe false
                page1.hasNext shouldBe true
                page2.hasPrevious shouldBe true
                page2.hasNext shouldBe false
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
