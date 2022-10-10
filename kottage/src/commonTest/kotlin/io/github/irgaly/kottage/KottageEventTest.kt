package io.github.irgaly.kottage

import app.cash.turbine.test
import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import io.github.irgaly.kottage.extension.buildKottage
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

/**
 * Event 関連のテスト
 */
class KottageEventTest : DescribeSpec({
    val tempDirectory = tempdir()
    fun kottage(
        name: String = "test", builder: (KottageOptions.Builder.() -> Unit)? = null
    ): Pair<Kottage, TestCalendar> = buildKottage(name, tempDirectory, builder)

    val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
    describe("Kottage Event Test") {
        val kottage = Kottage(
            "test",
            tempDirectory,
            KottageEnvironment(KottageContext(), calendar)
        )
        kottage.storage("initialize").put("initialize", "initialize") // 初期化
        context("debug 機能") {
            it("tempDirectory 表示") {
                println("tempDirectory = $tempDirectory")
            }
        }
        context("Storage 操作") {
            it("並列で書き込みがあっても正しく Event が記録されていること") {
                val storage = kottage.storage("storage")
                val jobs = mutableListOf<Job>()
                repeat(100) { id ->
                    jobs.add(launch {
                        storage.put("key$id", "value$id")
                        storage.remove("key$id")
                    })
                }
                jobs.joinAll()
                storage.getEvents(0).size shouldBe 200
            }
            it("maxEventEntryCount を超えたら削除される") {
                val storage = kottage.storage("storage2") {
                    maxEventEntryCount = 10
                }
                repeat(11) { id ->
                    storage.put("key$id", "value$id")
                }
                storage.getEvents(0).size shouldBe 9
            }
            it("古い Event は削除される") {
                val storage = kottage.storage("event_expire") {
                    eventExpireTime = 10.days
                }
                calendar.setUtc(DateTime(2021, 1, 1))
                storage.put("key", "value")
                calendar.setUtc(DateTime(2021, 1, 11))
                storage.compact()
                storage.getEvents(0).size shouldBe 0
            }
        }
        context("Event") {
            it("Simple Event を受け取れること") {
                val calendar2 = TestCalendar(DateTime(2022, 1, 1).utc)
                val kottage2 = Kottage(
                    "event",
                    tempDirectory,
                    KottageEnvironment(KottageContext(), calendar2)
                )
                val storage = kottage2.cache("event")
                kottage2.simpleEventFlow.test {
                    calendar2.now += 1.seconds
                    storage.put("key1", "value")
                    awaitItem().eventType shouldBe KottageEventType.Create
                    calendar2.now += 1.seconds
                    storage.remove("key1")
                    awaitItem().eventType shouldBe KottageEventType.Delete
                }
            }
            it("List Event を受け取れること") {
                val (kottage, calendar) = kottage("event_list")
                val cache = kottage.cache("event_list")
                val list = cache.list("list_event_list")
                cache.eventFlow().filter { it.listType != null }.test {
                    list.add("key1", "value1")
                    awaitItem().let {
                        it.listPositionId shouldNotBe null
                        it.listType shouldBe "list_event_list"
                        it.eventType shouldBe KottageEventType.Create
                    }
                    calendar.now += 1.seconds
                    list.remove(checkNotNull(list.getFirst()).positionId)
                    awaitItem().let {
                        it.listPositionId shouldNotBe null
                        it.listType shouldBe "list_event_list"
                        it.eventType shouldBe KottageEventType.Delete
                    }
                }
            }
        }
        context("KottageEventFlow") {
            it("KottageEventFlowで値を監視できる") {
                val calendar2 = TestCalendar(DateTime(2022, 1, 1).utc)
                val kottage2 = Kottage(
                    "eventflow",
                    tempDirectory,
                    KottageEnvironment(KottageContext(), calendar2)
                )
                calendar2.now += 1.seconds
                val storage = kottage2.cache("event")
                storage.put("key1", "value")
                storage.eventFlow(DateTime(2022, 1, 1).unixMillisLong).test {
                    // 2022/1/1 以降のイベントから流れる
                    awaitItem().eventType shouldBe KottageEventType.Create
                    storage.remove("key1")
                    awaitItem().eventType shouldBe KottageEventType.Delete
                }
            }
        }
    }
})
