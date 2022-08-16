package net.irgaly.kkvs.internal.repository

import com.squareup.sqldelight.db.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.irgaly.kkvs.data.sqlite.KkvsDatabase
import net.irgaly.kkvs.data.sqlite.extension.executeAsExists
import net.irgaly.kkvs.internal.model.Item

internal class KkvsSqliteItemRepository(
    private val database: KkvsDatabase,
    private val itemType: String
) : KkvsItemRepository {
    init {
        check(itemType.contains("+")) {
            "itemType should not contains \"+\": itemType = \"$itemType\""
        }
    }

    override suspend fun upsert(item: Item) = withContext(Dispatchers.Default) {
        database.itemQueries
            .replace(item.toEntity())
    }

    override suspend fun updateLastRead(key: String, lastReadAt: Long) =
        withContext(Dispatchers.Default) {
            database.itemQueries
                .updateLastRead(lastReadAt, key)
        }

    override suspend fun updateExpireAt(key: String, expireAt: Long) =
        withContext(Dispatchers.Default) {
            database.itemQueries
                .updateExpireAt(expireAt, key)
        }

    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        database.itemQueries
            .select(key(key))
            .executeAsExists()
    }

    override suspend fun get(key: String): Item? = withContext(Dispatchers.Default) {
        database.itemQueries
            .select(key(key))
            .executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getAllKeys(receiver: suspend (key: String) -> Unit) {
        database.itemQueries
            .selectAllKeys(itemType)
            .execute().use { cursor ->
                runBlocking {
                    while (cursor.next()) {
                        val key = checkNotNull(cursor.getString(0))
                        receiver(key)
                    }
                }
            }
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.Default) {
        database.itemQueries
            .delete(key(key))
    }

    override suspend fun deleteAll() {
        database.itemQueries
            .deleteAllType(itemType)
    }

    private fun key(key: String): String {
        return "${itemType}+${key}"
    }
}
