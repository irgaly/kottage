package io.github.irgaly.kottage.internal.platform

import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.database.DatabaseConnectionFactory
import io.github.irgaly.kottage.internal.repository.KottageRepositoryFactory

internal expect class PlatformFactory() {
    fun createKottageRepositoryFactory(databaseConnection: DatabaseConnection): KottageRepositoryFactory
    fun createDatabaseConnectionFactory(): DatabaseConnectionFactory
}
