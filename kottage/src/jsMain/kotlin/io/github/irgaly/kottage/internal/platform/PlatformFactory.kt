package io.github.irgaly.kottage.internal.platform

import io.github.irgaly.kottage.internal.database.DatabaseConnection
import io.github.irgaly.kottage.internal.database.DatabaseConnectionFactory
import io.github.irgaly.kottage.internal.database.IndexeddbDatabaseConnectionFactory
import io.github.irgaly.kottage.internal.repository.KottageIndexeddbRepositoryFactory
import io.github.irgaly.kottage.internal.repository.KottageRepositoryFactory
import kotlinx.browser.window

internal actual class PlatformFactory {
    actual fun createKottageRepositoryFactory(databaseConnection: DatabaseConnection): KottageRepositoryFactory {
        @Suppress("SENSELESS_COMPARISON")
        return if (window != null) {
            KottageIndexeddbRepositoryFactory()
        } else {
            TODO()
        }
    }

    actual fun createDatabaseConnectionFactory(): DatabaseConnectionFactory {
        @Suppress("SENSELESS_COMPARISON")
        return if (window != null) {
            IndexeddbDatabaseConnectionFactory()
        } else {
            TODO()
        }
    }
}
