package net.irgaly.kkvs.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.irgaly.kkvs.KkvsEnvironment
import net.irgaly.kkvs.data.sqlite.DriverFactory
import net.irgaly.kkvs.data.sqlite.KkvsDatabase
import net.irgaly.kkvs.data.sqlite.extension.executeAsExists
import net.irgaly.kkvs.internal.model.Item

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

    override suspend fun upsert(item: Item) = withContext(Dispatchers.Default) {
        database.itemQueries
            .replaceItem(
                net.irgaly.kkvs.data.sqlite.Item(
                    key = item.key,
                    type = item.type,
                    string_value = item.stringValue,
                    long_value = item.longValue,
                    double_value = item.doubleValue,
                    bytes_value = item.bytesValue,
                    created_at = item.createdAt,
                    last_read_at = item.lastReadAt,
                    expire_at = item.expireAt
                )
            )
    }

    override suspend fun updateLastRead(key: String, lastReadAt: Long) {
        database.itemQueries
            .updateLastRead(lastReadAt, key)
    }

    override suspend fun updateExpireAt(key: String, expireAt: Long) {
        database.itemQueries
            .updateExpireAt(expireAt, key)
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



