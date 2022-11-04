package io.github.irgaly.kottage.data.sqlite

import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.SqlPreparedStatement
import io.github.irgaly.kottage.data.sqlite.external.Database
import io.github.irgaly.kottage.data.sqlite.external.prepareStatement
import io.github.irgaly.kottage.data.sqlite.external.run
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import io.github.irgaly.kottage.data.sqlite.external.IteratorReturnResult
import io.github.irgaly.kottage.data.sqlite.external.Statement

class NodejsSqlDriver(
    val db: Database
): SqlDriver {
    private var transaction: Transacter.Transaction? = null

    override fun executeQuery(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): SqlCursor {
        return createOrGetStatement(identifier, sql).run {
            bind(binders)
            NodejsSqlCursor(this)
        }
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?) {
        return createOrGetStatement(identifier, sql).run {
            bind(binders)
            run()
        }
    }

    private fun Statement<Array<Any>>.bind(binders: (SqlPreparedStatement.() -> Unit)?) {
        binders?.let {
            val bound = NodejsSqlPreparedStatement()
            binders(bound)
            bind(bound.parameters.filterNotNull().toTypedArray())
        }
    }

    private fun createOrGetStatement(
        @Suppress("UNUSED_PARAMETER")
        identifier: Int?,
        sql: String
    ): Statement<Array<Any>> {
        return db.prepareStatement(sql)
    }

    override fun newTransaction(): Transacter.Transaction {
        val enclosing = transaction
        val transaction = Transaction(enclosing)
        this.transaction = transaction
        if (enclosing == null) {
            db.prepareStatement("BEGIN TRANSACTION").run()
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

private class NodejsSqlCursor(statement: Statement<Array<Any>>) : SqlCursor {
    val iterator = statement.iterate()
    var currentRow: Array<dynamic>? = null
    override fun next(): Boolean {
        val result = iterator.next().unsafeCast<IteratorReturnResult<Array<dynamic>>>()
        currentRow = result.value
        return result.done
    }
    override fun getString(index: Int): String? = currentRow?.get(index)?.unsafeCast<String>()
    override fun getLong(index: Int): Long? = currentRow?.get(index)?.unsafeCast<Double>()?.toLong()
    override fun getBytes(index: Int): ByteArray? = (currentRow?.get(index)?.unsafeCast<Uint8Array>())?.let {
        Int8Array(it.buffer).unsafeCast<ByteArray>()
    }
    override fun getDouble(index: Int): Double? = currentRow?.get(index)?.unsafeCast<Double>()
    override fun close() {}
}

private class NodejsSqlPreparedStatement : SqlPreparedStatement {

    val parameters = mutableListOf<Any?>()

    override fun bindBytes(index: Int, bytes: ByteArray?) {
        parameters.add(bytes?.toTypedArray())
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
