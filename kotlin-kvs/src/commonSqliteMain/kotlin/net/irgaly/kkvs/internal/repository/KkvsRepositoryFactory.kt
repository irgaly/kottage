package net.irgaly.kkvs.internal.repository

import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.KkvsEnvironment
import net.irgaly.kkvs.data.sqlite.DriverFactory

internal actual class KkvsRepositoryFactory actual constructor(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
) {
    private val driver: SqlDriver by lazy {
        DriverFactory(environment.context).createDriver(fileName, directoryPath)
    }

    actual fun create(itemType: String): KkvsItemRepository {
        return KkvsSqliteItemRepository(driver, itemType)
    }
}
