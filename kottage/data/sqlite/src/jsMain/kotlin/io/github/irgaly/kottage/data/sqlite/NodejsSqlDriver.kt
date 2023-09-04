package io.github.irgaly.kottage.data.sqlite

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import io.github.irgaly.kottage.data.sqlite.external.Database
import io.github.irgaly.kottage.data.sqlite.external.IteratorReturnResult
import io.github.irgaly.kottage.data.sqlite.external.Statement
import io.github.irgaly.kottage.data.sqlite.external.prepareStatement
import io.github.irgaly.kottage.data.sqlite.external.run
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

class NodejsSqlDriver(
    val db: Database
) : SqlDriver {
    private var transaction: Transacter.Transaction? = null
    private val listeners = mutableMapOf<String, MutableSet<Query.Listener>>()

    init {
        db.unsafeMode(true)
    }

    override fun addListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach {
            listeners.getOrPut(it) { mutableSetOf() }.add(listener)
        }
    }

    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach {
            listeners[it]?.remove(listener)
        }
    }

    override fun notifyListeners(vararg queryKeys: String) {
        queryKeys.flatMap { listeners[it].orEmpty() }
            .distinct()
            .forEach(Query.Listener::queryResultsChanged)
    }

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<R> {
        val cursor = createOrGetStatement(identifier, sql, true).run {
            bind(parameters, binders)
            NodejsSqlCursor(this)
        }
        return try {
            mapper(cursor)
        } finally {
            cursor.close()
        }
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> {
        return createOrGetStatement(identifier, sql, false).run {
            bind(parameters, binders)
            val result = run()
            QueryResult.Value(result.changes.toLong())
        }
    }

    private fun Statement<Array<Any?>>.bind(
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ) {
        binders?.let {
            val bound = NodejsSqlPreparedStatement(parameters)
            binders(bound)
            bind(bound.parameters.toTypedArray())
        }
    }

    private fun createOrGetStatement(
        @Suppress("UNUSED_PARAMETER")
        identifier: Int?,
        sql: String,
        query: Boolean
    ): Statement<Array<Any?>> {
        return db.prepareStatement(sql, query)
    }

    override fun newTransaction(): QueryResult<Transacter.Transaction> {
        val enclosing = transaction
        val transaction = Transaction(enclosing)
        this.transaction = transaction
        if (enclosing == null) {
            db.run("BEGIN TRANSACTION")
        }
        return QueryResult.Value(transaction)
    }

    override fun currentTransaction(): Transacter.Transaction? {
        return transaction
    }

    override fun close() {
        db.close()
    }

    private inner class Transaction(
        override val enclosingTransaction: Transacter.Transaction?
    ) : Transacter.Transaction() {
        override fun endTransaction(successful: Boolean): QueryResult<Unit> {
            if (enclosingTransaction == null) {
                if (successful) {
                    db.run("END TRANSACTION")
                } else {
                    db.run("ROLLBACK TRANSACTION")
                }
            }
            transaction = enclosingTransaction
            return QueryResult.Unit
        }
    }
}

private class NodejsSqlCursor(statement: Statement<Array<Any?>>) : SqlCursor {
    val iterator = statement.iterate()
    var currentRow: Array<dynamic>? = null
    override fun next(): QueryResult.Value<Boolean> {
        val result = iterator.next().unsafeCast<IteratorReturnResult<Array<dynamic>>>()
        currentRow = result.value
        return QueryResult.Value(!result.done)
    }

    override fun getString(index: Int): String? {
        return currentRow?.get(index)?.unsafeCast<String>()
    }

    override fun getLong(index: Int): Long? {
        return currentRow?.get(index)?.unsafeCast<JsBigInt>()?.toString()?.toLong()
    }

    override fun getBytes(index: Int): ByteArray? {
        return (currentRow?.get(index)?.unsafeCast<Uint8Array>())?.let {
            Int8Array(it.buffer).unsafeCast<ByteArray>()
        }
    }

    override fun getDouble(index: Int): Double? {
        return currentRow?.get(index)?.unsafeCast<Double>()
    }

    override fun getBoolean(index: Int): Boolean? {
        return currentRow?.get(index)?.unsafeCast<Double>()?.let {
            (it.toLong() == 1L)
        }
    }

    fun close() {
        @Suppress("UNUSED_VARIABLE")
        val localIterator = iterator
        js("localIterator.return()")
    }
}

private class NodejsSqlPreparedStatement(parameters: Int) : SqlPreparedStatement {

    val parameters = MutableList<Any?>(parameters) { null }

    override fun bindBytes(index: Int, bytes: ByteArray?) {
        parameters[index] = bytes?.let {
            Uint8Array(it.toTypedArray())
        }
    }

    override fun bindLong(index: Int, long: Long?) {
        parameters[index] = long?.let { BigInt(it.toString()) }
    }

    override fun bindDouble(index: Int, double: Double?) {
        parameters[index] = double
    }

    override fun bindString(index: Int, string: String?) {
        parameters[index] = string
    }

    override fun bindBoolean(index: Int, boolean: Boolean?) {
        parameters[index] = when (boolean) {
            null -> null
            true -> 1.0
            false -> 0.0
        }
    }
}

private external fun BigInt(value: String): JsBigInt

@JsName("BigInt")
private external class JsBigInt
