package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.data.sqlite.extension.executeAsExists
import io.github.irgaly.kottage.internal.model.Item

internal class KkvsSqliteItemRepository(
    private val database: KottageDatabase,
    private val itemType: String
) : KkvsItemRepository {
    init {
        require(!itemType.contains("+")) {
            "itemType should not contains \"+\": itemType = \"$itemType\""
        }
    }

    override fun upsert(item: Item) {
        database.itemQueries
            .replace(item.toEntity())
    }

    override fun updateLastRead(key: String, lastReadAt: Long) {
        database.itemQueries
            .updateLastRead(lastReadAt, Item.toEntityKey(key, itemType))
    }

    override fun updateExpireAt(key: String, expireAt: Long) {
        database.itemQueries
            .updateExpireAt(expireAt, Item.toEntityKey(key, itemType))
    }

    override fun exists(key: String): Boolean {
        return database.itemQueries
            .selectKey(Item.toEntityKey(key, itemType))
            .executeAsExists()
    }

    override fun get(key: String): Item? {
        return database.itemQueries
            .select(Item.toEntityKey(key, itemType))
            .executeAsOneOrNull()?.toDomain()
    }

    override fun getCount(): Long {
        return database.itemQueries
            .countByType(itemType)
            .executeAsOne()
    }

    override fun getAllKeys(receiver: (key: String) -> Unit) {
        return database.itemQueries
            .selectAllKeys(itemType)
            .execute().use { cursor ->
                while (cursor.next()) {
                    val key = checkNotNull(cursor.getString(0))
                    receiver(Item.fromEntityKey(key, itemType))
                }
            }
    }

    override fun delete(key: String) {
        database.itemQueries
            .delete(Item.toEntityKey(key, itemType))
    }

    override fun deleteLeastRecentlyUsed(limit: Long) {
        database.itemQueries
            .selectLeastRecentlyUsedKeys(itemType, limit)
            .execute().use { cursor ->
                while (cursor.next()) {
                    val key = checkNotNull(cursor.getString(0))
                    database.itemQueries
                        .delete(key)
                }
            }
    }

    override fun deleteOlderItems(limit: Long) {
        database.itemQueries
            .selectOlderCreatedKeys(itemType, limit)
            .execute().use { cursor ->
                while (cursor.next()) {
                    val key = checkNotNull(cursor.getString(0))
                    database.itemQueries
                        .delete(key)
                }
            }
    }

    override fun deleteAll() {
        database.itemQueries
            .deleteAllByType(itemType)
    }

    override fun getStatsCount(): Long {
        return database.item_statsQueries
            .select(itemType)
            .executeAsOneOrNull()?.count ?: 0
    }

    override fun incrementStatsCount(count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .incrementCount(count, itemType)
    }

    override fun decrementStatsCount(count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .decrementCount(count, itemType)
    }

    override fun updateStatsCount(count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .updateCount(count, itemType)
    }

    override fun deleteStats() {
        database.item_statsQueries
            .delete(itemType)
    }
}
