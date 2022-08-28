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
import kotlinx.serialization.Serializable

class KottageTest : DescribeSpec({
    val tempDirectory = tempdir()
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
    describe("Kottage") {
        val kottage = Kottage(
            "test",
            tempDirectory,
            KottageEnvironment(Context(), calendar)
        )
        context("debug 機能") {
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
            val subdirectoryKottage = Kottage(
                "test",
                directory,
                KottageEnvironment(Context(), calendar)
            )
            val storage = subdirectoryKottage.storage("storage1")
            it("ディレクトリが存在しなくてもファイルを作成できる") {
                shouldNotThrowAny {
                    storage.put("key", "test")
                }
            }
        }
        context("storage モード") {
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
                        val storage = kottage.storage("storage1")
                        storage.put("key$id", "value$id")
                        val value = storage.get<String>("key$id")
                        value shouldBe "value$id"
                    }
                }
            }
        }
        context("基本的な型の操作") {
            val storage = kottage.storage("storage_basic_type")
            it("Double, Float") {
                storage.put("double", 0.0)
                storage.put("float", 0f)
                val doubleDoubleValue = storage.get<Double>("double")
                val doubleFloatValue = storage.get<Float>("double")
                val floatFloatValue = storage.get<Float>("float")
                val floatDoubleValue = storage.get<Double>("float")
                doubleDoubleValue shouldBe 0.0
                doubleFloatValue shouldBe 0f
                floatFloatValue shouldBe 0f
                floatDoubleValue shouldBe 0.0
            }
            it("Long, Int, Short, Byte, Boolean") {
                storage.put("long", 0L)
                storage.put("int", 0)
                storage.put("short", 0.toShort())
                storage.put("byte", 0.toByte())
                storage.put("boolean", false)
                storage.get<Long>("long") shouldBe 0L
                storage.get<Int>("int") shouldBe 0
                storage.get<Byte>("byte") shouldBe 0.toByte()
                storage.get<Boolean>("boolean") shouldBe false
                storage.get<Long>("boolean") shouldBe 0L
                storage.get<Int>("byte") shouldBe 0
                storage.get<Byte>("int") shouldBe 0.toByte()
                storage.get<Boolean>("long") shouldBe false
            }
            it("ByteArray") {
                storage.put("bytearray", byteArrayOf(0, 1))
                storage.get<ByteArray>("bytearray") shouldBe byteArrayOf(0, 1)
            }
            it("String") {
                storage.put("string", "test")
                storage.get<String>("string") shouldBe "test"
            }
            it("Serializable") {
                @Serializable
                data class Data(val data: Int)
                storage.put("serializable", Data(0))
                storage.get<Data>("serializable") shouldBe Data(0)
                storage.get<String>("serializable") shouldBe "{\"data\":0}"
            }
            it("List<Primitive>") {
                storage.put("list", listOf("test"))
                storage.get<List<String>>("list") shouldBe listOf("test")
                storage.get<String>("list") shouldBe "[\"test\"]"
            }
        }
    }
})
