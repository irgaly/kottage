package io.github.irgaly.kottage.data.sqlite.internal

import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.SqlPreparedStatement
import com.squareup.sqldelight.drivers.sqljs.QueryResults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.MessageEvent
import org.w3c.dom.Worker
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private fun <T> jsObject(block: T.() -> Unit): T {
    val o = js("{}").unsafeCast<T>()
    block(o)
    return o
}

fun initSqlDriver(
    workerPath: String = "/worker.sql-wasm.js",
    schema: SqlDriver.Schema? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): SqlDriver = JsWorkerSqlDriver(Worker(workerPath), dispatcher).withSchema(schema)

fun SqlDriver.withSchema(schema: SqlDriver.Schema? = null): SqlDriver {
    return this.also {
        schema?.create(it)
    }
}

/**
 * from https://github.com/cashapp/sqldelight/blob/42767f89895f7dad3ff0b68170a3d849519bcc18/drivers/sqljs-driver/src/main/kotlin/app/cash/sqldelight/driver/sqljs/worker/JsWorkerSqlDriver.kt
 */
@OptIn(DelicateCoroutinesApi::class)
class JsWorkerSqlDriver(
    private val worker: Worker,
    private val dispatcher: CoroutineDispatcher
) : SqlDriver {
    private var messageCounter = 0
    private var transaction: Transacter.Transaction? = null

    override fun executeQuery(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): SqlCursor {
        val bound = JsWorkerSqlPreparedStatement()
        binders?.invoke(bound)

        val messageId = messageCounter++
        val message = jsObject<WorkerMessage> {
            this.id = messageId
            this.action = "exec"
            this.sql = sql
            this.params = bound.parameters.toTypedArray()
        }

        var result: SqlCursor? = null
        GlobalScope.launch(dispatcher) {
            val data = worker.sendMessage(messageId, message).unsafeCast<WorkerData>()
            val table = if (data.results.isNotEmpty()) {
                data.results[0]
            } else {
                jsObject { values = arrayOf() }
            }
            result = JsWorkerSqlCursor(table, {})
        }
        // SQLDelight 2.0 なら async Worker で処理が書ける
        while(result == null) {}
        return result!!
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ) {
        val bound = JsWorkerSqlPreparedStatement()
        binders?.invoke(bound)

        val messageId = messageCounter++
        val message = jsObject<WorkerMessage> {
            this.id = messageId
            this.action = "exec"
            this.sql = sql
            this.params = bound.parameters.toTypedArray()
        }

        GlobalScope.launch(dispatcher) {
            worker.sendMessage(messageId, message)
        }
    }

    override fun close() = worker.terminate()

    override fun newTransaction(): Transacter.Transaction {
        val enclosing = transaction
        val transaction = Transaction(enclosing)
        this.transaction = transaction
        if (enclosing == null) {
            GlobalScope.launch(dispatcher) {
                worker.run("BEGIN TRANSACTION")
            }
        }

        return transaction
    }

    override fun currentTransaction(): Transacter.Transaction? = transaction

    private inner class Transaction(
        override val enclosingTransaction: Transacter.Transaction?,
    ) : Transacter.Transaction() {
        override fun endTransaction(successful: Boolean) {
            if (enclosingTransaction == null) {
                GlobalScope.launch(dispatcher) {
                    if (successful) {
                        worker.run("END TRANSACTION")
                    } else {
                        worker.run("ROLLBACK TRANSACTION")
                    }
                }
            }
            transaction = enclosingTransaction
        }
    }

    private suspend fun Worker.sendMessage(id: Int, message: dynamic): WorkerData = suspendCancellableCoroutine { continuation ->
        val messageListener = object : EventListener {
            override fun handleEvent(event: Event) {
                val data = event.unsafeCast<MessageEvent>().data.unsafeCast<WorkerData>()
                if (data.id == id) {
                    removeEventListener("message", this)
                    if (data.error != null) {
                        continuation.resumeWithException(JsWorkerException(data.error!!))
                    } else {
                        continuation.resume(data)
                    }
                }
            }
        }

        val errorListener = object : EventListener {
            override fun handleEvent(event: Event) {
                removeEventListener("error", this)
                continuation.resumeWithException(JsWorkerException(event.toString()))
            }
        }

        addEventListener("message", messageListener)
        addEventListener("error", errorListener)

        postMessage(message)

        continuation.invokeOnCancellation {
            removeEventListener("message", messageListener)
            removeEventListener("error", errorListener)
        }
    }

    private suspend fun Worker.run(sql: String) {
        val messageId = messageCounter++
        val message = jsObject<WorkerMessage> {
            this.id = messageId
            this.action = "exec"
            this.sql = sql
        }

        sendMessage(messageId, message)
    }
}

class JsWorkerSqlCursor(
    private val table: QueryResults,
    private val onClose: () -> Unit
) : SqlCursor {
    private var currentRow = -1

    override fun next(): Boolean = ++currentRow < table.values.size

    override fun getString(index: Int): String? = table.values[currentRow][index]

    override fun getLong(index: Int): Long? = (table.values[currentRow][index] as? Double)?.toLong()

    override fun getBytes(index: Int): ByteArray? = (table.values[currentRow][index] as? Uint8Array)?.let { Int8Array(it.buffer).unsafeCast<ByteArray>() }

    override fun getDouble(index: Int): Double? = table.values[currentRow][index]

    override fun close() {
        onClose()
    }
}

internal class JsWorkerSqlPreparedStatement : SqlPreparedStatement {

    val parameters = mutableListOf<Any?>()

    override fun bindBytes(index: Int, bytes: ByteArray?) {
        parameters.add(bytes?.toTypedArray())
    }

    override fun bindLong(index: Int, long: Long?) {
        // We convert Long to Double because Kotlin's Double is mapped to JS number
        // whereas Kotlin's Long is implemented as a JS object
        parameters.add(long?.toDouble())
    }

    override fun bindDouble(index: Int, double: Double?) {
        parameters.add(double)
    }

    override fun bindString(index: Int, string: String?) {
        parameters.add(string)
    }
}
