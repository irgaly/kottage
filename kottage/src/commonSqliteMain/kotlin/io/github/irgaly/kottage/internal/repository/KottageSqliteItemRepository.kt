package io.github.irgaly.kottage.internal.repository

import com.squareup.sqldelight.db.use
import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.data.sqlite.extension.executeAsExists
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal class KottageSqliteItemRepository(
    private val database: KottageDatabase
) : KottageItemRepository {
    override fun upsert(transaction: Transaction, item: Item) {
        database.itemQueries
            .replace(item.toEntity())
    }

    override fun updateLastRead(
        transaction: Transaction,
        key: String,
        itemType: String,
        lastReadAt: Long
    ) {
        database.itemQueries
            .updateLastRead(lastReadAt, Item.toEntityKey(key, itemType))
    }

    override fun updateExpireAt(
        transaction: Transaction,
        key: String,
        itemType: String,
        expireAt: Long
    ) {
        database.itemQueries
            .updateExpireAt(expireAt, Item.toEntityKey(key, itemType))
    }

    override fun exists(transaction: Transaction, key: String, itemType: String): Boolean {
        return database.itemQueries
            .selectKey(Item.toEntityKey(key, itemType))
            .executeAsExists()
    }

    override fun get(transaction: Transaction, key: String, itemType: String): Item? {
        return database.itemQueries
            .select(Item.toEntityKey(key, itemType))
            .executeAsOneOrNull()?.toDomain()
    }

    override fun getCount(transaction: Transaction, itemType: String): Long {
        return database.itemQueries
            .countByType(itemType)
            .executeAsOne()
    }

    override fun getAllKeys(
        transaction: Transaction,
        itemType: String,
        receiver: (key: String) -> Unit
    ) {
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
        transaction: Transaction,
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
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: (key: String) -> Boolean
    ) {
        if (limit != null) {
            database.itemQueries
                .selectLeastRecentlyUsedKeysLimit(type = itemType, limit = limit)
                .execute().use { cursor ->
                    var canNext = true
                    while (canNext && cursor.next()) {
                        val key = checkNotNull(cursor.getString(0))
                        canNext = receiver(Item.keyFromEntityKey(key, itemType))
                    }
                }
        } else {
            database.itemQueries
                .selectLeastRecentlyUsedKeys(type = itemType)
                .execute().use { cursor ->
                    var canNext = true
                    while (canNext && cursor.next()) {
                        val key = checkNotNull(cursor.getString(0))
                        canNext = receiver(Item.keyFromEntityKey(key, itemType))
                    }
                }
        }
    }

    override fun getOlderKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: (key: String) -> Boolean
    ) {
        if (limit != null) {
            database.itemQueries
                .selectOlderCreatedKeysLimit(type = itemType, limit = limit)
                .execute().use { cursor ->
                    var canNext = true
                    while (canNext && cursor.next()) {
                        val key = checkNotNull(cursor.getString(0))
                        canNext = receiver(Item.keyFromEntityKey(key, itemType))
                    }
                }
        } else {
            database.itemQueries
                .selectOlderCreatedKeys(type = itemType)
                .execute().use { cursor ->
                    var canNext = true
                    while (canNext && cursor.next()) {
                        val key = checkNotNull(cursor.getString(0))
                        canNext = receiver(Item.keyFromEntityKey(key, itemType))
                    }
                }
        }
    }

    override fun getStats(transaction: Transaction, itemType: String): ItemStats? {
        return database.item_statsQueries
            .select(item_type = itemType)
            .executeAsOneOrNull()?.toDomain()
    }

    override fun getEmptyStats(transaction: Transaction, limit: Long): List<ItemStats> {
        // クリーンアップ用途だけなので、selectEmptyStats Query はインデックスを使わない
        return database.item_statsQueries
            .selectEmptyStats(limit = limit)
            .executeAsList().map { it.toDomain() }
    }

    override fun delete(transaction: Transaction, key: String, itemType: String) {
        database.itemQueries
            .delete(Item.toEntityKey(key, itemType))
    }

    override fun deleteAll(transaction: Transaction, itemType: String) {
        database.itemQueries
            .deleteAllByType(itemType)
    }

    override fun getStatsCount(transaction: Transaction, itemType: String): Long {
        return database.item_statsQueries
            .select(itemType)
            .executeAsOneOrNull()?.count ?: 0
    }

    override fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .incrementCount(count, itemType)
    }

    override fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .decrementCount(count, itemType)
    }

    override fun updateStatsCount(transaction: Transaction, itemType: String, count: Long) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .updateCount(count, itemType)
    }

    override fun deleteStats(transaction: Transaction, itemType: String) {
        database.item_statsQueries
            .delete(itemType)
    }
}
