package io.github.irgaly.kottage.data.indexeddb.extension

@Suppress("UnsafeCastFromDynamic")
fun <T : Any> jso(): T = js("({})")
inline fun <T : Any> jso(block: T.() -> Unit): T = jso<T>().apply(block)
