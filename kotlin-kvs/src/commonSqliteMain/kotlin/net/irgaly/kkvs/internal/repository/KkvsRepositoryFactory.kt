package net.irgaly.kkvs.internal.repository

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.KkvsEnvironment
import net.irgaly.kkvs.data.sqlite.DriverFactory
import net.irgaly.kkvs.data.sqlite.Item_event
import net.irgaly.kkvs.data.sqlite.KkvsDatabase

internal actual class KkvsRepositoryFactory actual constructor(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    private val driver: SqlDriver by lazy {
        DriverFactory(environment.context).createDriver(fileName, directoryPath)
    }

    private val database: KkvsDatabase by lazy {
        KkvsDatabase(driver, Item_event.Adapter(EnumColumnAdapter()))
    }

    actual fun <R> transactionWithResult(bodyWithReturn: () -> R): R {
        return database.itemQueries.transactionWithResult {
            bodyWithReturn()
        }
    }

    actual fun transaction(body: () -> Unit) {
        return database.itemQueries.transaction {
            body()
        }
    }

    actual fun createItemRepository(itemType: String): KkvsItemRepository {
        return KkvsSqliteItemRepository(database, itemType)
    }

    actual fun createItemEventRepository(): KkvsItemEventRepository {
        return KkvsSqliteItemEventRepository(database)
    }
}
