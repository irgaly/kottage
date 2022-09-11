package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * Event 関連のテスト
 */
class KottageEventTest : DescribeSpec({
    val tempDirectory = tempdir()
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
        }
    }
})
