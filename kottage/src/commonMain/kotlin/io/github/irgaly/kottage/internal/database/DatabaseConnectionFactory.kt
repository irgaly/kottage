package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.KottageEnvironment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal interface DatabaseConnectionFactory {
    /**
     * @throws IllegalArgumentException invalid fileName: contains file separator
     */
    @Throws(IllegalArgumentException::class)
    fun createDatabaseConnection(
        fileName: String,
        directoryPath: String,
        environment: KottageEnvironment,
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): DatabaseConnection

    suspend fun createOldDatabase(
        fileName: String,
        directoryPath: String,
        environment: KottageEnvironment,
        version: Int,
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    )
}
