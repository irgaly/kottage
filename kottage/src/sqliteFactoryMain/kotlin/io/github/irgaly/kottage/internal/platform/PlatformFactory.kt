package io.github.irgaly.kottage.internal.platform

import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.database.DatabaseConnectionFactory
import io.github.irgaly.kottage.internal.database.SqliteDatabaseConnectionFactory
import io.github.irgaly.kottage.internal.repository.KottageRepositoryFactory
import io.github.irgaly.kottage.internal.repository.KottageSqliteRepositoryFactory

internal actual class PlatformFactory {
    actual fun createKottageRepositoryFactory(databaseConnection: DatabaseConnection): KottageRepositoryFactory {
        return KottageSqliteRepositoryFactory(databaseConnection)
    }

    actual fun createDatabaseConnectionFactory(): DatabaseConnectionFactory {
        return SqliteDatabaseConnectionFactory()
    }
}
