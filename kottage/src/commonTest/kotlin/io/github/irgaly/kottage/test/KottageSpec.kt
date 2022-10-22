package io.github.irgaly.kottage.test

import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.extension.buildKottage
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.test.extension.tempdir
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestScope

/**
 * kotest JS ではネストしたテストや DescribeSpec は使えない
 * DescribeSpec の書き方でフラットなテストとなるようにブリッジする
 * テストは it の中でのみ実行可能
 * すべての it の実行順は保証しない
 */
open class KottageSpec(
    val name: String = "kottage",
    clearTempDirectoryAfterSpec: Boolean = false,
    body: (KottageSpec.() -> Unit) = {}
) : FunSpec() {
    // kotest が KottageSpec を実行しようとするが、zero arg constructor がないとエラーになるので
    // dummy constructor
    constructor() : this("KottageSpec::dummy")

    val tempDirectory: String

    init {
        tempDirectory = tempdir(clearTempDirectoryAfterSpec)
        context("debug 機能", fun ContextScope.() {
            it("tempDirectory 表示") {
                println("tempDirectory = $tempDirectory")
            }
        })
        body()
    }

    fun kottage(
        name: String = this.name,
        builder: (KottageOptions.Builder.() -> Unit)? = null
    ): Pair<Kottage, TestCalendar> = buildKottage(name, tempDirectory, builder)

    fun it(name: String, test: suspend TestScope.() -> Unit) {
        test(name, test)
    }

    fun xit(name: String, test: suspend TestScope.() -> Unit) {
        xtest(name, test)
    }

    fun context(name: String, test: ContextScope.() -> Unit) {
        test(ContextScope(name))
    }

    @Suppress("UNUSED_PARAMETER")
    fun xcontext(name: String, test: ContextScope.() -> Unit) {
        xtest(name)
    }

    fun describe(name: String, test: DescribeScope.() -> Unit) {
        test(DescribeScope(name))
    }

    @Suppress("UNUSED_PARAMETER")
    fun xdescribe(name: String, test: DescribeScope.() -> Unit) {
        xtest(name)
    }

    inner class ContextScope(val contextName: String) {
        fun it(name: String, test: suspend TestScope.() -> Unit) {
            test("$contextName: $name", test)
        }

        fun xit(name: String, test: suspend TestScope.() -> Unit) {
            xtest("$contextName: $name", test)
        }
    }

    inner class DescribeScope(val describeName: String) {
        fun it(name: String, test: suspend TestScope.() -> Unit) {
            test("$describeName: $name", test)
        }

        fun xit(name: String, test: suspend TestScope.() -> Unit) {
            xtest("$describeName: $name", test)
        }

        fun context(name: String, test: ContextScope.() -> Unit) {
            test(ContextScope("$describeName: $name"))
        }

        @Suppress("UNUSED_PARAMETER")
        fun xcontext(name: String, test: ContextScope.() -> Unit) {
            xtest("$describeName: $name")
        }
    }
}
