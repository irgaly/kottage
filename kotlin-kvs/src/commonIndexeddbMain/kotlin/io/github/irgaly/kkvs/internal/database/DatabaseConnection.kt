package io.github.irgaly.kkvs.internal.database

import io.github.irgaly.kkvs.KkvsEnvironment

internal actual class DatabaseConnection {
    actual suspend fun <R> transactionWithResult(bodyWithReturn: suspend () -> R): R {
        TODO("Not yet implemented")
    }

    actual suspend fun transaction(body: suspend () -> Unit) {
        TODO("Not yet implemented")
    }

    actual suspend fun deleteAll() {
        TODO("Not yet implemented")
    }
}

internal actual fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
): DatabaseConnection {
    TODO()
}

