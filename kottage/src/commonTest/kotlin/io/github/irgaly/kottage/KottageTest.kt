package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import io.github.irgaly.kottage.encoder.KottageEncoder
import io.github.irgaly.kottage.extension.buildKottage
import io.github.irgaly.kottage.platform.Files
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.kottage.property.KottageStore
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.experimental.xor

class KottageTest : KottageSpec("kottage", body = {
    describe("Kottage") {
        context("debug 機能") {
            it("getDatabaseStatus() で情報を取得できる") {
                val status = kottage().first.getDatabaseStatus()
                println(status)
                status.shouldNotBeEmpty()
            }
            it("compact() をエラーなく実行できる") {
                shouldNotThrowAny {
                    kottage().first.compact()
                }
            }
            it("clear() をエラーなく実行できる") {
                val kottage = kottage("clear").first
                kottage.cache("cache").put("test", "test")
                shouldNotThrowAny {
                    kottage.clear()
                }
            }
            it("export() でバックアップを作成できる") {
                kottage().first.export("backup.db", tempDirectory)
            }
            it("export() で存在しないディレクトリにバックアップを作成できる") {
                kottage().first.export("backup.db", "$tempDirectory/backup")
            }
            it("export() で特殊なファイル名を扱える") {
                kottage().first.export(
                    "export_${
                        "_'_\"_/_\\_ _あ_😄_:_;_".replace(Files.separator, "-")
                    }.db", "$tempDirectory/${"_'_\"_/_\\_ _あ_😄_:_".replace(Files.separator, "-")}"
                )
            }
            it("export() で separator を含むファイル名はエラー") {
                shouldThrow<IllegalArgumentException> {
                    kottage().first.export("export_/_:_\\_.db", tempDirectory)
                }
            }
            it("特殊文字を含むパスを扱える") {
                shouldNotThrowAny {
                    Kottage(
                        "test",
                        "$tempDirectory/${
                            "_'_\"_/_\\_ _あ_😄_:_".replace(
                                Files.separator,
                                "-"
                            )
                        }",
                        KottageEnvironment(
                            KottageContext(),
                            TestCalendar(DateTime(2022, 1, 1).utc)
                        )
                    ).storage("test").put("test", "test")
                }
            }
            it("特殊文字を含むファイル名を扱える") {
                shouldNotThrowAny {
                    Kottage(
                        "test_'_\"_/_\\_ _あ_😄_:_".replace(Files.separator, "-"),
                        tempDirectory,
                        KottageEnvironment(
                            KottageContext(),
                            TestCalendar(DateTime(2022, 1, 1).utc)
                        )
                    ).storage("test").put("test", "test")
                }
            }
            it("separator を含むファイル名でエラー") {
                shouldThrow<IllegalArgumentException> {
                    Kottage(
                        "test_/_:_\\_",
                        tempDirectory,
                        KottageEnvironment(
                            KottageContext(),
                            TestCalendar(DateTime(2022, 1, 1).utc)
                        )
                    )
                }
            }
        }
        context("Connection") {
            it("ディレクトリが存在しなくてもファイルを作成できる") {
                val directory = "$tempDirectory/subdirectory"
                val subdirectoryKottage = buildKottage("test", directory).first
                val storage = subdirectoryKottage.storage("storage1")
                shouldNotThrowAny {
                    storage.put("key", "test")
                }
            }
            it("close() 後は IllegalStateException") {
                val kottage = kottage("connection_close").first
                val storage = kottage.storage("test")
                storage.put("test", "test")
                kottage.closed shouldBe false
                kottage.close()
                kottage.closed shouldBe true
                shouldThrow<IllegalStateException> {
                    storage.get<String>("test")
                }
            }
        }
        context("storage モード") {
            val storage = kottage().first.storage("storage1")
            it("put, get で値を保持できている") {
                storage.put("key", "test")
                storage.get<String>("key") shouldBe "test"
            }
        }
        context("独立 Kottage インスタンス") {
            it("並列書き込み: 100") {
                val initialKottage = kottage("test100").first
                // SQLite Table 作成
                initialKottage.storage("storage1").put("test", "test")
                repeat(100) { id ->
                    launch(Dispatchers.Default) {
                        val kottage2 = kottage("test100").first
                        val storage = kottage2.storage("storage1")
                        storage.put("key$id", "value$id")
                        storage.get<String>("key$id") shouldBe "value$id"
                    }
                }
            }
        }
        context("基本的な型の操作") {
            val storage = kottage().first.storage("storage_basic_type")
            it("Double, Float") {
                storage.put("double", 0.0)
                storage.put("float", 0f)
                storage.get<Double>("double") shouldBe 0.0
                storage.get<Float>("double") shouldBe 0f
                storage.get<Float>("float") shouldBe 0f
                storage.get<Double>("float") shouldBe 0.0
            }
            it("Max Min: Double, Float") {
                storage.put("double_min", Double.MIN_VALUE)
                storage.put("double_max", Double.MAX_VALUE)
                storage.put("float_min", Float.MIN_VALUE)
                storage.put("float_max", Float.MAX_VALUE)
                storage.get<Double>("double_min") shouldBe Double.MIN_VALUE
                storage.get<Double>("double_max") shouldBe Double.MAX_VALUE
                storage.get<Float>("float_min") shouldBe Float.MIN_VALUE
                storage.get<Float>("float_max") shouldBe Float.MAX_VALUE
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
            it("Min Max: Long, Int, Short, Byte") {
                storage.put("long_min", Long.MIN_VALUE)
                storage.put("long_max", Long.MAX_VALUE)
                storage.put("long_min_10", (Long.MIN_VALUE + 10)) // nodejs BigInt テスト
                storage.put("long_max_10", (Long.MAX_VALUE - 10))
                storage.put("int_min", Int.MIN_VALUE)
                storage.put("int_max", Int.MAX_VALUE)
                storage.put("short_min", Short.MIN_VALUE)
                storage.put("short_max", Short.MAX_VALUE)
                storage.put("byte_min", Byte.MIN_VALUE)
                storage.put("byte_max", Byte.MAX_VALUE)
                storage.get<Long>("long_min") shouldBe Long.MIN_VALUE
                storage.get<Long>("long_max") shouldBe Long.MAX_VALUE
                storage.get<Long>("long_min_10") shouldBe (Long.MIN_VALUE + 10)
                storage.get<Long>("long_max_10") shouldBe (Long.MAX_VALUE - 10)
                storage.get<Long>("int_min") shouldBe Int.MIN_VALUE
                storage.get<Long>("int_max") shouldBe Int.MAX_VALUE
                storage.get<Long>("short_min") shouldBe Short.MIN_VALUE
                storage.get<Long>("short_max") shouldBe Short.MAX_VALUE
                storage.get<Long>("byte_min") shouldBe Byte.MIN_VALUE
                storage.get<Long>("byte_max") shouldBe Byte.MAX_VALUE
            }
            it("ByteArray") {
                storage.put("bytearray", byteArrayOf(0, 1))
                storage.put("bytearray_min_max", byteArrayOf(Byte.MIN_VALUE, Byte.MAX_VALUE))
                storage.get<ByteArray>("bytearray") shouldBe byteArrayOf(0, 1)
                storage.get<ByteArray>("bytearray_min_max") shouldBe byteArrayOf(
                    Byte.MIN_VALUE,
                    Byte.MAX_VALUE
                )
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
            it("型不一致でエラー") {
                @Serializable
                data class Data(val data: Int)

                @Serializable
                data class Data2(val data2: Int)
                storage.put("long_to_string", 0)
                storage.put("serializable_error", Data(0))
                shouldThrow<ClassCastException> {
                    storage.get<String>("long_to_string")
                }
                shouldThrow<SerializationException> {
                    storage.get<Data2>("serializable_error")
                }
            }
            it("key が存在しない") {
                shouldThrow<NoSuchElementException> {
                    storage.get<String>("unknown_key")
                }
                storage.getOrNull<String>("unknown_key") shouldBe null
            }
        }
        context("property") {
            it("property で読み書きできる") {
                val storage = kottage().first.storage("property")
                val store by storage.property { "default" }
                val nullableStore: KottageStore<String?> by storage.nullableProperty()
                store.exists() shouldBe true
                store.read() shouldBe "default"
                nullableStore.exists() shouldBe false
                nullableStore.read() shouldBe null
                store.write("test")
                nullableStore.write("test")
                storage.exists("store") shouldBe true
                storage.exists("nullableStore") shouldBe true
                store.read() shouldBe "test"
                nullableStore.read() shouldBe "test"
                store.clear()
                nullableStore.clear()
                storage.exists("store") shouldBe false
                storage.exists("nullableStore") shouldBe false
                store.read() shouldBe "default"
                nullableStore.read() shouldBe null
            }
        }
        context("UserEncoder") {
            it("UserEncoder を指定して正しく保存・復元できる") {
                @Serializable
                data class Data(val value: String)

                val storage = kottage().first.storage("user_encoder") {
                    encoder = object : KottageEncoder {
                        override fun encode(value: ByteArray): ByteArray {
                            return value.map { it xor 0xF0.toByte() }.toByteArray()
                        }

                        override fun decode(encoded: ByteArray): ByteArray {
                            return encoded.map { it xor 0xF0.toByte() }.toByteArray()
                        }
                    }
                }
                storage.put("long", 100L)
                storage.put("int", 100)
                storage.put("short", 100.toShort())
                storage.put("byte", 100.toByte())
                storage.put("boolean", false)
                storage.put("double", 100.0)
                storage.put("float", 100.0f)
                storage.put("bytes", byteArrayOf(0, 1))
                storage.put("string", "string")
                storage.put("serializable", Data("data"))
                storage.get<Long>("long") shouldBe 100L
                storage.get<Int>("int") shouldBe 100
                storage.get<Short>("short") shouldBe 100.toShort()
                storage.get<Byte>("byte") shouldBe 100.toByte()
                storage.get<Boolean>("boolean") shouldBe false
                storage.get<Double>("double") shouldBe 100.0
                storage.get<Float>("float") shouldBe 100.0f
                storage.get<ByteArray>("bytes") shouldBe byteArrayOf(0, 1)
                storage.get<String>("string") shouldBe "string"
                storage.get<Data>("serializable") shouldBe Data("data")
            }
        }
        context("KottageOptions") {
            it("ignoreJsonDeserializationError でエラーを無視できる") {
                @Serializable
                data class Data1(val data1: String)

                @Serializable
                data class Data2(val data2: String)

                val kottage = kottage().first
                val storage = kottage.storage("ignore_json_error")
                val ignoreStorage = kottage.storage("ignore_json_error") {
                    ignoreJsonDeserializationError = true
                }
                storage.put("json", Data1("value1"))
                shouldThrow<SerializationException> {
                    storage.getOrNull<Data2>("json")
                }
                shouldThrow<NoSuchElementException> {
                    ignoreStorage.get<Data2>("json")
                }
                shouldThrow<SerializationException> {
                    // KottageEntry は ignoreJsonDeserializationError 設定の影響は受けない
                    ignoreStorage.getEntry<Data2>("json").get()
                }
                ignoreStorage.getOrNull<Data2>("json") shouldBe null
                ignoreStorage.getOrNull<Data1>("json") shouldBe Data1("value1")
            }
        }
    }
})
