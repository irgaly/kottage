@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "unused"
)

package io.github.irgaly.kottage.data.sqlite.external

external interface SharedArrayBuffer {
    var byteLength: Number
    var length: Number
    fun slice(begin: Number, end: Number = definedExternally): SharedArrayBuffer
}

external interface SharedArrayBufferConstructor {
    var prototype: SharedArrayBuffer
}
