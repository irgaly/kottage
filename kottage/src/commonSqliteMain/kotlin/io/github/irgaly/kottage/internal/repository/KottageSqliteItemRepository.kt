package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.data.sqlite.extension.executeAsExists
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal class KottageSqliteItemRepository(
    private val database: KottageDatabase
) : KottageItemRepository {
    override fun upsert(item: Item) {
        database.itemQueries
            .replace(item.toEntity())
    }

    override fun updateLastRead(key: String, itemType: String, lastReadAt: Long) {
        database.itemQueries
            .updateLastRead(lastReadAt, Item.toEntityKey(key, itemType))
    }

    override fun updateExpireAt(key: String, itemType: String, expireAt: Long) {
        database.itemQueries
            .updateExpireAt(expireAt, Item.toEntityKey(key, itemType))
    }

    override fun exists(key: String, itemType: String): Boolean {
        return database.itemQueries
            .selectKey(Item.toEntityKey(key, itemType))
            .executeAsExists()
    }

    override fun get(key: String, itemType: String): Item? {
        return database.itemQueries
            .select(Item.toEntityKey(key, itemType))
            .executeAsOneOrNull()?.toDomain()
    }

    override fun getCount(itemType: String): Long {
        return database.itemQueries
            .countByType(itemType)
            .executeAsOne()
    }

    override fun getAllKeys(itemType: String, receiver: (key: String) -> Unit) {
        database.itemQueries
            .selectAllKeys(itemType)
            .execute().use { cursor ->
                while (cursor.next()) {
                    val key = checkNotNull(cursor.getString(0))
                    receiver(Item.keyFromEntityKey(key, itemType))
                }
            }
    }

    override fun getExpiredKeys(
        now: Long,
        itemType: String?,
        receiver: (key: String, itemType: String) -> Unit
    ) {
        if (itemType != null) {
            database.itemQueries
                .selectExpiredKeys(itemType, now)
                .execute().use { cursor ->
                    while (cursor.next()) {
                        val key = checkNotNull(cursor.getString(0))
                        receiver(Item.keyFromEntityKey(key, itemType), itemType)
                    }
                }
        } else {
            database.itemQueries
                .selectAllTypeExpiredKeys(now)
                .execute().use { cursor ->
                    while (cursor.next()) {
                        val key = checkNotNull(cursor.getString(0))
                        val type = checkNotNull(cursor.getString(1))
                        receiver(Item.keyFromEntityKey(key, type), type)
                    }
                }
        }
    }

    override fun getLeastRecentlyUsedKeys(
        itemType: String,
        limit: Long,
        receiver: (key: String, itemType: String) -> Unit
    ) {
        database.itemQueries
            .selectLeastRecentlyUsedKeys(itemType, limit)
            .execute().use { cursor ->
                while (cursor.next()) {
                    val key = checkNotNull(cursor.getString(0))
                    receiver(Item.keyFromEntityKey(key, itemType), itemType)
                }
            }
    }

    override fun getOlderKeys(
        itemType: String,
        limit: Long,
        receiver: (key: String, itemType: String) -> Unit
    ) {
        database.itemQueries
            .selectOlderCreatedKeys(itemType, limit)
            .execute().use { cursor ->
                while (cursor.next()) {
                    val key = checkNotNull(cursor.getString(0))
                    receiver(Item.keyFromEntityKey(key, itemType), itemType)
                }
            }
    }

    override fun getStats(itemType: String): ItemStats? {
        return database.item_statsQueries
            .select(item_type = itemType)
            .executeAsOneOrNull()?.toDomain()
    }

    override fun delete(key: String, itemType: String) {
        database.itemQueries
            .delete(Item.toEntityKey(key, itemType))
    }

    override fun deleteAll(itemType: String) {
        database.itemQueries
            .deleteAllByType(itemType)
    }

    override fun getStatsCount(itemType: String): Long {
        return database.item_statsQueries
            .select(itemType)
            .executeAsOneOrNull()?.count ?: 0
    }

    override fun incrementStatsCount(itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .incrementCount(count, itemType)
    }

    override fun decrementStatsCount(itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .decrementCount(count, itemType)
    }

    override fun updateStatsCount(itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .updateCount(count, itemType)
    }

    override fun deleteStats(itemType: String) {
        database.item_statsQueries
            .delete(itemType)
    }
}
