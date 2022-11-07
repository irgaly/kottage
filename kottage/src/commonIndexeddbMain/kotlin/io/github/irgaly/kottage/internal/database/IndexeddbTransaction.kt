package io.github.irgaly.kottage.internal.database

import com.juul.indexeddb.WriteTransaction

internal class IndexeddbTransaction(
    val transaction: WriteTransaction
) : Transaction
