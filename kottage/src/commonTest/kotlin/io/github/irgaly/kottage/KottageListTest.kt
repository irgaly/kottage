package io.github.irgaly.kottage

import io.github.irgaly.kottage.extension.buildKottage
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class KottageListTest : DescribeSpec({
    val tempDirectory = tempdir()
    val printListStatus = true
    fun kottage(
        name: String = "test", builder: (KottageOptions.Builder.() -> Unit)? = null
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
                        kottageListEntry("key3", "value3"),
                        kottageListEntry("key4", "value4")
                    )
                )
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
                cache.get<String>("key3") shouldBe "value3"
                cache.get<String>("key4") shouldBe "value4"
                list.getFirst()?.entry<String>()?.get() shouldBe "value1"
                list.getByIndex(1)?.entry<String>()?.get() shouldBe "value2"
                list.getByIndex(2)?.entry<String>()?.get() shouldBe "value3"
                list.getByIndex(3)?.entry<String>()?.get() shouldBe "value4"
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
                val first = checkNotNull(list.getFirst())
                list.remove(first.positionId)
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
                list.getFirst()?.entry<String>()?.get() shouldBe "value2"
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
                val item = checkNotNull(list.getFirst())
                item.info shouldBe "info"
                item.previousKey shouldBe "prev"
                item.currentKey shouldBe "current"
                item.nextKey shouldBe "next"
                if (printListStatus) {
                    println(list.getDebugStatus())
                    println(list.getDebugListRawData())
                }
            }
        }
        }
    }
})
