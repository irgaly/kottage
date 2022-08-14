package net.irgaly.kkvs.internal.repository

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.irgaly.kkvs.data.sqlite.Item_event
import net.irgaly.kkvs.data.sqlite.KkvsDatabase
import net.irgaly.kkvs.data.sqlite.extension.executeAsExists
import net.irgaly.kkvs.internal.model.Item

class KkvsSqliteItemRepository(
    private val driver: SqlDriver,
    private val itemType: String
) : KkvsItemRepository {
    private val database: KkvsDatabase by lazy {
        KkvsDatabase(driver, Item_event.Adapter(EnumColumnAdapter()))
    }

    init {
        check(itemType.contains("+")) {
            "itemType should not contains \"+\": itemType = \"$itemType\""
        }
    }

    override suspend fun upsert(item: Item) = withContext(Dispatchers.Default) {
        database.itemQueries
            .replace(
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
            .select(key(key))
            .executeAsExists()
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.Default) {
        database.itemQueries
            .delete(key(key))
    }

    private fun key(key: String): String {
        return "${itemType}+${key}"
    }
}
