@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "unused",
    "UNUSED_TYPEALIAS_PARAMETER"
)

package io.github.irgaly.kottage.data.sqlite.external

typealias VariableArgFunction = (params: Any) -> Any

typealias ArgumentTypes<F> = Any

typealias BetterSqlite3SqliteError = Error
