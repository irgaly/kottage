package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.KkvsEnvironment
import kotlinx.coroutines.CoroutineDispatcher

internal actual class DatabaseConnection {
    actual suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R {
        TODO("Not yet implemented")
    }

    actual suspend fun transaction(body: () -> Unit) {
        TODO("Not yet implemented")
    }

    actual suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    actual suspend fun getDatabaseStatus(): String {
        TODO("Not yet implemented")
    }

    actual suspend fun backupTo(file: String, directoryPath: String) {
        TODO("Not yet implemented")
    }

    actual suspend fun compact() {
        TODO("Not yet implemented")
    }
}

internal actual fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
    dispatcher: CoroutineDispatcher
): DatabaseConnection {
    TODO()
}

