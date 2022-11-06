package io.github.irgaly.kottage.data.sqlite

import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.SqlPreparedStatement
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

    init {
        db.unsafeMode(true)
    }

    override fun executeQuery(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): SqlCursor {
        return createOrGetStatement(identifier, sql, true).run {
            bind(binders)
            NodejsSqlCursor(this)
        }
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ) {
        return createOrGetStatement(identifier, sql, false).run {
            bind(binders)
            run()
        }
    }

    private fun Statement<Array<Any?>>.bind(binders: (SqlPreparedStatement.() -> Unit)?) {
        binders?.let {
            val bound = NodejsSqlPreparedStatement()
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

    override fun newTransaction(): Transacter.Transaction {
        val enclosing = transaction
        val transaction = Transaction(enclosing)
        this.transaction = transaction
        if (enclosing == null) {
            db.run("BEGIN TRANSACTION")
        }
        return transaction
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
        override fun endTransaction(successful: Boolean) {
            if (enclosingTransaction == null) {
                if (successful) {
                    db.run("END TRANSACTION")
                } else {
                    db.run("ROLLBACK TRANSACTION")
                }
            }
            transaction = enclosingTransaction
        }
    }
}

private class NodejsSqlCursor(statement: Statement<Array<Any?>>) : SqlCursor {
    val iterator = statement.iterate()
    var currentRow: Array<dynamic>? = null
    override fun next(): Boolean {
        val result = iterator.next().unsafeCast<IteratorReturnResult<Array<dynamic>>>()
        currentRow = result.value
        return !result.done
    }

    override fun getString(index: Int): String? {
        return currentRow?.get(index)?.unsafeCast<String>()
    }

    override fun getLong(index: Int): Long? {
        return currentRow?.get(index)?.unsafeCast<Number>()?.toLong()
    }

    override fun getBytes(index: Int): ByteArray? {
        return (currentRow?.get(index)?.unsafeCast<Uint8Array>())?.let {
            Int8Array(it.buffer).unsafeCast<ByteArray>()
        }
    }

    override fun getDouble(index: Int): Double? {
        return currentRow?.get(index)?.unsafeCast<Double>()
    }

    override fun close() {
        @Suppress("UNUSED_VARIABLE")
        val localIterator = iterator
        js("localIterator.return()")
    }
}

private class NodejsSqlPreparedStatement : SqlPreparedStatement {

    val parameters = mutableListOf<Any?>()

    override fun bindBytes(index: Int, bytes: ByteArray?) {
        parameters.add(bytes?.let {
            Uint8Array(it.toTypedArray())
        })
    }

    override fun bindLong(index: Int, long: Long?) {
        parameters.add(long?.toDouble())
    }

    override fun bindDouble(index: Int, double: Double?) {
        parameters.add(double)
    }

    override fun bindString(index: Int, string: String?) {
        parameters.add(string)
    }
}
