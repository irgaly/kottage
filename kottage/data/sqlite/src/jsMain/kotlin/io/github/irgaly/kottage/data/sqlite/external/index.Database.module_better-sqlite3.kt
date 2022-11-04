@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS",
    "unused"
)
package io.github.irgaly.kottage.data.sqlite.external

import kotlin.js.*

external interface RunResult {
    var changes: Number
    var lastInsertRowid: dynamic /* Number | Any */
        get() = definedExternally
        set(value) = definedExternally
}

external interface Options {
    var readonly: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fileMustExist: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var timeout: Number?
        get() = definedExternally
        set(value) = definedExternally
    var verbose: ((message: Any, additionalArgs: Any) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var nativeBinding: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface SerializeOptions {
    var attached: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PragmaOptions {
    var simple: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface RegistrationOptions {
    var varargs: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var deterministic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var safeIntegers: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var directOnly: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface AggregateOptions : RegistrationOptions {
    var start: Any?
        get() = definedExternally
        set(value) = definedExternally
    var step: (total: Any, next: Any) -> Any
    var inverse: ((total: Any, dropped: Any) -> Any)?
        get() = definedExternally
        set(value) = definedExternally
    var result: ((total: Any) -> Any)?
        get() = definedExternally
        set(value) = definedExternally
}

external interface BackupMetadata {
    var totalPages: Number
    var remainingPages: Number
}

external interface BackupOptions {
    var progress: (info: BackupMetadata) -> Number
}

typealias SqliteError = Any
