package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class KottageListTest : DescribeSpec({
    val tempDirectory = tempdir()
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
    describe("Kottage List Test") {
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
        context("List 基本操作") {
            val cache = kottage.cache("list_basic")
            val list = cache.list("list_list_basic")
            it("List への add, get") {
                list.add("key1", "value1")
                list.add("key2", "value2")
                list.addAll(
                    listOf(
                        KottageListEntry("key3", "value3"),
                        KottageListEntry("key4", "value4")
                    )
                )
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
                cache.get<String>("key3") shouldBe "value3"
                cache.get<String>("key4") shouldBe "value4"
                list.getFirst<String>()?.entry?.get() shouldBe "value1"
                list.getByIndex<String>(1)?.entry?.get() shouldBe "value2"
                list.getByIndex<String>(2)?.entry?.get() shouldBe "value3"
                list.getByIndex<String>(3)?.entry?.get() shouldBe "value4"
            }
        }
    }
})