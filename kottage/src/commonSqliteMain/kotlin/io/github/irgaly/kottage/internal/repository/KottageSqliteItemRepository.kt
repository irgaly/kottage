package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.data.sqlite.extension.executeAsExists
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal class KottageSqliteItemRepository(
    private val database: KottageDatabase
) : KottageItemRepository {
    override suspend fun upsert(transaction: Transaction, item: Item) {
        database.itemQueries
            .replace(item.toEntity())
    }

    override suspend fun updateLastRead(
        transaction: Transaction,
        key: String,
        itemType: String,
        lastReadAt: Long
    ) {
        database.itemQueries
            .updateLastRead(lastReadAt, Item.toEntityKey(key, itemType))
    }

    override suspend fun updateExpireAt(
        transaction: Transaction,
        key: String,
        itemType: String,
        expireAt: Long
    ) {
        database.itemQueries
            .updateExpireAt(expireAt, Item.toEntityKey(key, itemType))
    }

    override suspend fun exists(
        transaction: Transaction,
        key: String,
        itemType: String
    ): Boolean {
        return database.itemQueries
            .selectKey(Item.toEntityKey(key, itemType))
            .executeAsExists()
    }

    override suspend fun get(transaction: Transaction, key: String, itemType: String): Item? {
        return database.itemQueries
            .select(Item.toEntityKey(key, itemType))
            .executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getCount(transaction: Transaction, itemType: String): Long {
        return database.itemQueries
            .countByType(itemType)
            .executeAsOne()
    }

    override suspend fun getAllKeys(
        transaction: Transaction,
        itemType: String,
        receiver: suspend (key: String) -> Unit
    ) {
        database.itemQueries
            .selectAllKeys(itemType)
            .executeAsList()
            .forEach { key ->
                receiver(Item.keyFromEntityKey(key, itemType))
            }
    }

    override suspend fun getExpiredKeys(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: suspend (key: String, itemType: String) -> Unit
    ) {
        if (itemType != null) {
            database.itemQueries
                .selectExpiredKeys(itemType, now)
                .executeAsList().forEach { key ->
                    receiver(Item.keyFromEntityKey(key, itemType), itemType)
                }
        } else {
            database.itemQueries
                .selectAllTypeExpiredKeys(now)
                .executeAsList().forEach {
                    receiver(Item.keyFromEntityKey(it.key, it.type), it.type)
                }
        }
    }

    override suspend fun getLeastRecentlyUsedKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    ) {
        if (limit != null) {
            val keys = database.itemQueries
                .selectLeastRecentlyUsedKeysLimit(type = itemType, limit = limit)
                .executeAsList()
            for (key in keys) {
                val canNext = receiver(Item.keyFromEntityKey(key, itemType))
                if (!canNext) {
                    break
                }
            }
        } else {
            val keys = database.itemQueries
                .selectLeastRecentlyUsedKeys(type = itemType)
                .executeAsList()
            for (key in keys) {
                val canNext = receiver(Item.keyFromEntityKey(key, itemType))
                if (!canNext) {
                    break
                }
            }
        }
    }

    override suspend fun getOlderKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    ) {
        if (limit != null) {
            val keys = database.itemQueries
                .selectOlderCreatedKeysLimit(type = itemType, limit = limit)
                .executeAsList()
            for (key in keys) {
                val canNext = receiver(Item.keyFromEntityKey(key, itemType))
                if (!canNext) {
                    break
                }
            }
        } else {
            val keys = database.itemQueries
                .selectOlderCreatedKeys(type = itemType)
                .executeAsList()
            for (key in keys) {
                val canNext = receiver(Item.keyFromEntityKey(key, itemType))
                if (!canNext) {
                    break
                }
            }
        }
    }

    override suspend fun getStats(transaction: Transaction, itemType: String): ItemStats? {
        return database.item_statsQueries
            .select(item_type = itemType)
            .executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getEmptyStats(
        transaction: Transaction,
        limit: Long
    ): List<ItemStats> {
        // クリーンアップ用途だけなので、selectEmptyStats Query はインデックスを使わない
        return database.item_statsQueries
            .selectEmptyStats(limit = limit)
            .executeAsList().map { it.toDomain() }
    }

    override suspend fun delete(transaction: Transaction, key: String, itemType: String) {
        database.itemQueries
            .delete(Item.toEntityKey(key, itemType))
    }

    override suspend fun deleteAll(transaction: Transaction, itemType: String) {
        database.itemQueries
            .deleteAllByType(itemType)
    }

    override suspend fun getStatsCount(transaction: Transaction, itemType: String): Long {
        return database.item_statsQueries
            .select(itemType)
            .executeAsOneOrNull()?.count ?: 0
    }

    override suspend fun incrementStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .incrementCount(count, itemType)
    }

    override suspend fun decrementStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .decrementCount(count, itemType)
    }

    override suspend fun updateStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        database.item_statsQueries
            .insertIfNotExists(itemType)
        database.item_statsQueries
            .updateCount(count, itemType)
    }

    override suspend fun deleteStats(transaction: Transaction, itemType: String) {
        database.item_statsQueries
            .delete(itemType)
    }
}
