package io.github.irgaly.kottage.data.sqlite.extension

import com.squareup.sqldelight.Query

/**
 * execute query and result has one more items
 */
fun Query<*>.executeAsExists(): Boolean {
    return execute().next()
}
