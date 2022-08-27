package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import io.github.irgaly.kottage.platform.Context
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KottageTest : DescribeSpec({
    val tempDirectory = tempdir()
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
    describe("Kottage") {
        context("debug 機能") {
            val kottage = Kottage(
                "test",
                tempDirectory,
                KottageEnvironment(Context(), calendar)
            )
            it("tempDirectory 表示") {
                println("tempDirectory = $tempDirectory")
            }
            it("getDatabaseStatus() で情報を取得できる") {
                val status = kottage.getDatabaseStatus()
                println(status)
                status.shouldNotBeEmpty()
            }
            it("compact() をエラーなく実行できる") {
                shouldNotThrowAny {
                    kottage.compact()
                }
            }
            it("export() でバックアップを作成できる") {
                kottage.export("backup.db", tempDirectory)
            }
            it("export() で存在しないディレクトリにバックアップを作成できる") {
                kottage.export("backup.db", "$tempDirectory/backup")
            }
            // TODO:
            // シングルクォート、ダブルクォーテーション
            // バックスラッシュ、スペース、: の扱いを確認する
            // ファイル名に /, \, シングルクォート、ダブルクォーテーションの扱いを確認する
        }
        context("Connection") {
            val directory = "$tempDirectory/subdirectory"
            val kottage = Kottage(
                "test",
                directory,
                KottageEnvironment(Context(), calendar)
            )
            val storage = kottage.storage("storage1")
            it("ディレクトリが存在しなくてもファイルを作成できる") {
                shouldNotThrowAny {
                    storage.put("key", "test")
                }
            }
        }
        context("storage モード") {
            val kottage = Kottage(
                "test",
                tempDirectory,
                KottageEnvironment(Context(), calendar)
            )
            val storage = kottage.storage("storage1")
            it("put, get で値を保持できている") {
                storage.put("key", "test")
                val value: String = storage.get("key")
                value shouldBe "test"
            }
        }
        context("独立 Kottage インスタンス") {
            it("並列書き込み: 100") {
                repeat(100) { id ->
                    launch(Dispatchers.Default) {
                        val kottage = Kottage(
                            "test",
                            tempDirectory,
                            KottageEnvironment(Context(), calendar)
                        )
                        val storage = kottage.storage("storage1")
                        storage.put("key$id", "value$id")
                        val value = storage.get<String>("key$id")
                        value shouldBe "value$id"
                    }
                }
            }
        }
    }
})
