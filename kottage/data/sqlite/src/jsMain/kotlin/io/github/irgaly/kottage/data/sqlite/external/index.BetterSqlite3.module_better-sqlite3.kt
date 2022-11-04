@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS",
    "unused",
    "DEPRECATION",
    "UNUSED_TYPEALIAS_PARAMETER",
    "ClassName",
    "PropertyName"
)
package io.github.irgaly.kottage.data.sqlite.external

import kotlin.js.*

external interface Statement<BindParameters : Array<Any>> {
    var database: Database
    var source: String
    var reader: Boolean
    var busy: Boolean
    fun run(vararg params: BindParameters): RunResult
    fun get(vararg params: BindParameters): Any
    fun all(vararg params: BindParameters): Array<Any>
    fun iterate(vararg params: BindParameters): IterableIterator<Any>
    fun pluck(toggleState: Boolean = definedExternally): Statement<BindParameters> /* this */
    fun expand(toggleState: Boolean = definedExternally): Statement<BindParameters> /* this */
    fun raw(toggleState: Boolean = definedExternally): Statement<BindParameters> /* this */
    fun bind(vararg params: BindParameters): Statement<BindParameters> /* this */
    fun columns(): Array<ColumnDefinition>
    fun safeIntegers(toggleState: Boolean = definedExternally): Statement<BindParameters> /* this */
}

external interface ColumnDefinition {
    var name: String
    var column: String?
    var table: String?
    var database: String?
    var type: String?
}

external interface Transaction<F : VariableArgFunction> {
    @nativeInvoke
    operator fun invoke(vararg params: ArgumentTypes<F>): ReturnType<F>
    fun default(vararg params: ArgumentTypes<F>): ReturnType<F>
    fun deferred(vararg params: ArgumentTypes<F>): ReturnType<F>
    fun immediate(vararg params: ArgumentTypes<F>): ReturnType<F>
    fun exclusive(vararg params: ArgumentTypes<F>): ReturnType<F>
}

typealias ReturnType<T> = Any

external interface VirtualTableOptions {
    var rows: () -> Generator__0
    var columns: Array<String>
    var parameters: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var safeIntegers: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var directOnly: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

typealias Generator<T, TReturn, TNext> = Iterator<T, TReturn, TNext>

external interface Generator__0 : Generator<Any, Any, Any>

external interface GeneratorFunction {
    @nativeInvoke
    operator fun invoke(vararg args: Any): Generator__0
    var length: Number
    var name: String
    var prototype: Generator__0
}

external interface Database {
    var memory: Boolean
    var readonly: Boolean
    var name: String
    var open: Boolean
    var inTransaction: Boolean
    fun prepare(source: String): Any
    fun <F : VariableArgFunction> transaction(fn: F): Transaction<F>
    fun exec(source: String): Database /* this */
    fun pragma(source: String, options: PragmaOptions = definedExternally): Any
    fun function(name: String, cb: (params: Any) -> Any): Database /* this */
    fun function(name: String, options: RegistrationOptions, cb: (params: Any) -> Any): Database /* this */
    fun aggregate(name: String, options: AggregateOptions): Database /* this */
    fun loadExtension(path: String): Database /* this */
    fun close(): Database /* this */
    fun defaultSafeIntegers(toggleState: Boolean = definedExternally): Database /* this */
    fun backup(destinationFile: String, options: BackupOptions = definedExternally): Promise<BackupMetadata>
    fun table(name: String, options: VirtualTableOptions): Database /* this */
    fun unsafeMode(unsafe: Boolean = definedExternally): Database /* this */
    fun serialize(options: SerializeOptions = definedExternally): dynamic
}

external interface DatabaseConstructor {
    @nativeInvoke
    operator fun invoke(filename: String, options: Options = definedExternally): Database
    var prototype: Database
    var SqliteError: Any
}
