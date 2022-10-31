package io.github.irgaly.kottage.data.indexeddb.extension

import com.juul.indexeddb.Key
import com.juul.indexeddb.Queryable
import com.juul.indexeddb.Transaction

suspend fun Transaction.exists(store: Queryable, query: Key? = null): Boolean {
    return (0 < store.count(query))
}
