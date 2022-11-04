package io.github.irgaly.kottage.internal.database

import com.squareup.sqldelight.EnumColumnAdapter
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.data.sqlite.DriverFactory
import io.github.irgaly.kottage.data.sqlite.Item_event
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.data.sqlite.createDriver
import io.github.irgaly.kottage.platform.Files
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SqliteDatabaseConnectionFactory: DatabaseConnectionFactory {
    override fun createDatabaseConnection(
        fileName: String,
        directoryPath: String,
        environment: KottageEnvironment,
        dispatcher: CoroutineDispatcher
    ): DatabaseConnection {
        require(!fileName.contains(Files.separator)) { "fileName contains separator: $fileName" }
        return SqliteDatabaseConnection({
            if (!Files.exists(directoryPath)) {
                Files.mkdirs(directoryPath)
            }
            DriverFactory(
                environment.context.context,
                dispatcher
            ).createDriver(fileName, directoryPath)
        }, { sqlDriver ->
            KottageDatabase(sqlDriver, Item_event.Adapter(EnumColumnAdapter()))
        }, dispatcher)
    }

    override suspend fun createOldDatabase(
        fileName: String,
        directoryPath: String,
        environment: KottageEnvironment,
        version: Int,
        dispatcher: CoroutineDispatcher
    ) {
        withContext(dispatcher) {
            require(!fileName.contains(Files.separator)) { "fileName contains separator: $fileName" }
            DriverFactory(
                environment.context.context,
                dispatcher
            ).createDriver(fileName, directoryPath, version)
                .execute(null, "PRAGMA no_operation /* execution for opening connection */", 0)
        }
    }
}
