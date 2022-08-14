package net.irgaly.kkvs.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.irgaly.kkvs.KkvsEnvironment
import net.irgaly.kkvs.data.sqlite.DriverFactory
import net.irgaly.kkvs.data.sqlite.KkvsDatabase
import net.irgaly.kkvs.data.sqlite.extension.executeAsExists

class KkvsSqliteRepository(
    private val itemType: String,
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
): KkvsRepository {
    private val database: KkvsDatabase by lazy {
        KkvsDatabase(
            DriverFactory(environment.context).createDriver(fileName, directoryPath)
        )
    }

    init {
        check(itemType.contains("+")) {
            "itemType should not contains \"+\": itemType = \"$itemType\""
        }
    }

    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        database.itemQueries
            .selectItem(key(key))
            .executeAsExists()
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.Default) {
        database.itemQueries
            .deleteItem(key(key))
    }

    private inline fun key(key: String): String {
        return "${itemType}+${key}"
    }
}



