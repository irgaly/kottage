package net.irgaly.kkvs.internal

import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.irgaly.kkvs.Database
import net.irgaly.kkvs.internal.extension.executeAsExists

class KkvsSqliteRepository(
    private val itemType: String,
    driver: SqlDriver
): KkvsRepository {
    private val database: Database by lazy {
        Database(driver)
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



