@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS",
    "unused",
    "DEPRECATION"
)
package io.github.irgaly.kottage.data.sqlite.external

import kotlin.js.*

external interface SymbolConstructor {
    var matchAll: Any
    var asyncIterator: Any
    var hasInstance: Any
    var isConcatSpreadable: Any
    var match: Any
    var replace: Any
    var search: Any
    var species: Any
    var split: Any
    var toPrimitive: Any
    var toStringTag: Any
    var unscopables: Any
    var prototype: dynamic
    @nativeInvoke
    operator fun invoke(description: String = definedExternally): Any
    @nativeInvoke
    operator fun invoke(): Any
    @nativeInvoke
    operator fun invoke(description: Number = definedExternally): Any
    fun `for`(key: String): Any
    fun keyFor(sym: Any): String?
    var iterator: Any
}

external interface IteratorYieldResult<TYield> {
    var done: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var value: TYield
}

external interface IteratorReturnResult<TReturn> {
    var done: Boolean
    var value: TReturn
}

external interface Iterator<T, TReturn, TNext> {
    fun next(vararg args: Any /* JsTuple<> | JsTuple<TNext> */): dynamic /* IteratorYieldResult<T> | IteratorReturnResult<TReturn> */
    val `return`: ((value: TReturn) -> dynamic)?
    val `throw`: ((e: Any) -> dynamic)?
}

typealias Iterator__1<T> = Iterator<T, Any, Nothing?>

external interface Iterable<T>

external interface IterableIterator<T> : Iterator__1<T>

external interface PromiseConstructor {
    fun <T> allSettled(values: T): Promise<Any>
    fun <T> allSettled(values: Iterable<T>): Promise<Array<dynamic /* PromiseFulfilledResult<Any> | PromiseRejectedResult */>>
    var prototype: Promise<Any>
    fun all(values: Any /* JsTuple<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?> | JsTuple<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?> | JsTuple<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?> | JsTuple<Any?, Any?, Any?, Any?, Any?, Any?, Any?> | JsTuple<Any?, Any?, Any?, Any?, Any?, Any?> | JsTuple<Any?, Any?, Any?, Any?, Any?> | JsTuple<Any?, Any?, Any?, Any?> | JsTuple<Any?, Any?, Any?> | JsTuple<Any?, Any?> */): Promise<dynamic /* JsTuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> | JsTuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> | JsTuple<T1, T2, T3, T4, T5, T6, T7, T8> | JsTuple<T1, T2, T3, T4, T5, T6, T7> | JsTuple<T1, T2, T3, T4, T5, T6> | JsTuple<T1, T2, T3, T4, T5> | JsTuple<T1, T2, T3, T4> | JsTuple<T1, T2, T3> | JsTuple<T1, T2> */>
    fun <T> all(values: Array<Any? /* T | PromiseLike<T> */>): Promise<Array<T>>
    fun <T> race(values: Array<T>): Promise<Any>
    fun <T> reject(reason: Any = definedExternally): Promise<T>
    fun <T> resolve(value: T): Promise<T>
    fun <T> resolve(value: dynamic): Promise<T>
    fun resolve(): Promise<Unit>
    fun <T> all(values: Iterable<Any? /* T | PromiseLike<T> */>): Promise<Array<T>>
    fun <T> race(values: Iterable<T>): Promise<Any>
    fun <T> race(values: Iterable<Any? /* T | PromiseLike<T> */>): Promise<T>
}
