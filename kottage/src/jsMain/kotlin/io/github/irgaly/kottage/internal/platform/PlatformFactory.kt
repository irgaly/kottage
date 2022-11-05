package io.github.irgaly.kottage.internal.platform

import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.database.DatabaseConnectionFactory
import io.github.irgaly.kottage.internal.database.IndexeddbDatabaseConnectionFactory
import io.github.irgaly.kottage.internal.repository.KottageIndexeddbRepositoryFactory
import io.github.irgaly.kottage.internal.repository.KottageRepositoryFactory
import io.github.irgaly.kottage.platform.isBrowser

internal actual class PlatformFactory {
    actual fun createKottageRepositoryFactory(databaseConnection: DatabaseConnection): KottageRepositoryFactory {
        return if (isBrowser()) {
            KottageIndexeddbRepositoryFactory()
        } else {
            TODO()
        }
    }

    actual fun createDatabaseConnectionFactory(): DatabaseConnectionFactory {
        return if (isBrowser()) {
            IndexeddbDatabaseConnectionFactory()
        } else {
            TODO()
        }
    }
}
