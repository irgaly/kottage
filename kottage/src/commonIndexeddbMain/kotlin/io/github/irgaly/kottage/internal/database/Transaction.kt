package io.github.irgaly.kottage.internal.database

import com.juul.indexeddb.WriteTransaction

internal actual class Transaction(
    val transaction: WriteTransaction
)
