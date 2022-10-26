package io.github.irgaly.kottage.data.sqlite.internal

import com.squareup.sqldelight.drivers.sqljs.QueryResults

// from https://github.com/cashapp/sqldelight/blob/42767f89895f7dad3ff0b68170a3d849519bcc18/drivers/sqljs-driver/src/main/kotlin/app/cash/sqldelight/driver/sqljs/worker/WorkerMessages.kt

/**
 * Messages sent by the SQLDelight driver to the worker.
 */
internal external interface WorkerMessage {
    /**
     * A unique identifier used to identify responses to this message
     * @see WorkerData.id
     */
    var id: dynamic

    /**
     * The action that the worker should run. The worker driver only needs the "exec" action. See: [https://github.com/sql-js/sql.js/blob/master/src/worker.js]
     */
    var action: String

    /**
     * The SQL to execute
     */
    var sql: String

    /**
     * SQL parameters to bind to the given [sql]
     */
    var params: Array<Any?>
}

/**
 * Data returned by the worker after posting a message.
 */
internal external interface WorkerData {
    /**
     * An error returned by the worker, could be undefined.
     */
    var error: String?

    /**
     * The id of the message that this data is in response to. Matches the value that was posted in [WorkerMessage.id].
     * @see WorkerMessage.id
     */
    var id: dynamic

    /**
     * An array of [QueryResults] containing any of the rows that were returned by the query.
     * The array could be empty! In general, we only expect one QueryResult to be returned.
     */
    var results: Array<QueryResults>
}
