package io.github.irgaly.kottage

import app.cash.turbine.test
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import korlibs.time.DateTime
import korlibs.time.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Event 関連のテスト
 */
@OptIn(ExperimentalTime::class)
class KottageEventTest : KottageSpec("kottage_event", body = {
    describe("Kottage Event Test") {
        context("Storage 操作") {
            it("並列で書き込みがあっても正しく Event が記録されていること") {
                val storage = kottage("parallel").first.storage("storage")
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
                val storage = kottage("maxEventEntryCount").first.storage("storage2") {
                    maxEventEntryCount = 10
                }
                repeat(11) { id ->
                    storage.put("key$id", "value$id")
                }
                storage.getEvents(0).size shouldBe 9
            }
            it("古い Event は削除される") {
                val (kottage, calendar) = kottage("old_event")
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
                val (kottage, calendar) = kottage("event")
                val storage = kottage.cache("event")
                val storage2 = kottage.cache("event2")
                kottage.simpleEventFlow.test {
                    storage.put("key1", "value")
                    awaitItem().eventType shouldBe KottageEventType.Create
                    storage.remove("key1")
                    awaitItem().eventType shouldBe KottageEventType.Delete
                    storage.put("key2", "value2")
                    storage2.put("storage2_key", "value")
                    awaitItem().itemType shouldBe storage.name
                    awaitItem().itemType shouldBe storage2.name
                }
            }
            it("List Event を受け取れること") {
                val (kottage, calendar) = kottage("event_list")
                val cache = kottage.cache("event_list")
                val list = cache.list("list_event_list")
                cache.eventFlow().let { flow ->
                    flow.test {
                        flow.ensureSubscribed()
                        val entry1 = list.add("key1", "value1")
                        awaitItem() should {
                            it.itemKey shouldBe entry1.itemKey
                            it.listType shouldBe null
                            it.eventType shouldBe KottageEventType.Create
                        }
                        cache.put("key2", "value2")
                        awaitItem() should {
                            it.itemKey shouldBe "key2"
                            it.listType shouldBe null
                            it.eventType shouldBe KottageEventType.Create
                        }
                    }
                }
                list.eventFlow().let { flow ->
                    flow.test {
                        flow.ensureSubscribed()
                        val entry3 = list.add("key3", "value3")
                        awaitItem().let {
                            it.listPositionId shouldNotBe null
                            it.listType shouldBe "list_event_list"
                            it.eventType shouldBe KottageEventType.Create
                        }
                        cache.put("key3", "value3-2")
                        awaitItem().let {
                            it.listPositionId shouldNotBe null
                            it.listType shouldBe "list_event_list"
                            it.eventType shouldBe KottageEventType.Update
                        }
                        list.update(entry3.positionId, "key4", "value4")
                        awaitItem().let {
                            it.listPositionId shouldNotBe null
                            it.listType shouldBe "list_event_list"
                            it.itemKey shouldBe "key3"
                            it.eventType shouldBe KottageEventType.Delete
                        }
                        awaitItem().let {
                            it.listPositionId shouldNotBe null
                            it.listType shouldBe "list_event_list"
                            it.itemKey shouldBe "key4"
                            it.eventType shouldBe KottageEventType.Create
                        }
                        cache.put("key5", "value5")
                        list.updateKey(entry3.positionId, "key5")
                        awaitItem().let {
                            it.listPositionId shouldNotBe null
                            it.listType shouldBe "list_event_list"
                            it.itemKey shouldBe "key4"
                            it.eventType shouldBe KottageEventType.Delete
                        }
                        awaitItem().let {
                            it.listPositionId shouldNotBe null
                            it.listType shouldBe "list_event_list"
                            it.itemKey shouldBe "key5"
                            it.eventType shouldBe KottageEventType.Create
                        }
                        list.remove(entry3.positionId)
                        awaitItem().let {
                            it.listPositionId shouldNotBe null
                            it.listType shouldBe "list_event_list"
                            it.eventType shouldBe KottageEventType.Delete
                        }
                    }
                }
            }
        }
        context("KottageEventFlow") {
            it("KottageEventFlowで値を監視できる") {
                val (kottage, calendar) = kottage("eventflow")
                calendar.now += 1.seconds
                val storage = kottage.cache("event")
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
