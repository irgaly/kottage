package io.github.irgaly.kottage.data.sqlite.extension

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.use

/**
 * execute query and result has one more items
 */
fun Query<*>.executeAsExists(): Boolean {
    return execute().use { it.next() }
}
