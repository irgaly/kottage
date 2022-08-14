package net.irgaly.kkvs.internal

import co.touchlab.sqliter.DatabaseConfiguration
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection
import net.irgaly.kkvs.Database
import net.irgaly.kkvs.KkvsEnvironment

internal actual class DriverFactory actual constructor(private val environment: KkvsEnvironment) {
    actual fun createDriver(fileName: String, directoryPath: String): SqlDriver {
        val schema = Database.Schema
        return NativeSqliteDriver(
            DatabaseConfiguration(
                name = fileName,
                version = schema.version,
                create = { connection ->
                    wrapConnection(connection) { schema.create(it) }
                },
                upgrade = { connection, oldVersion, newVersion ->
                    wrapConnection(connection) { schema.migrate(it, oldVersion, newVersion) }
                },
                inMemory = false,
                extendedConfig = DatabaseConfiguration.Extended(
                    basePath = directoryPath
                )
            ),
        )
    }
}
