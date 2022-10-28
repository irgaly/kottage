package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal class KottageIndexeddbItemListRepository : KottageItemListRepository {
    override fun upsert(transaction: Transaction, entry: ItemListEntry) {
        TODO("Not yet implemented")
    }

    override fun updatePreviousId(transaction: Transaction, id: String, previousId: String?) {
        TODO("Not yet implemented")
    }

    override fun updateNextId(transaction: Transaction, id: String, nextId: String?) {
        TODO("Not yet implemented")
    }

    override fun updateItemKey(
        transaction: Transaction,
        id: String,
        itemType: String,
        itemKey: String?,
        expireAt: Long?
    ) {
        TODO("Not yet implemented")
    }

    override fun updateExpireAt(transaction: Transaction, id: String, expireAt: Long?) {
        TODO("Not yet implemented")
    }

    override fun removeItemKey(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override fun removeUserData(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override fun get(transaction: Transaction, id: String): ItemListEntry? {
        TODO("Not yet implemented")
    }

    override fun getIds(transaction: Transaction, itemType: String, itemKey: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun getInvalidatedItemIds(
        transaction: Transaction,
        type: String,
        beforeExpireAt: Long?,
        limit: Long
    ): List<String> {
        TODO("Not yet implemented")
    }

    override fun getCount(transaction: Transaction, type: String): Long {
        TODO("Not yet implemented")
    }

    override fun getInvalidatedItemCount(transaction: Transaction, type: String): Long {
        TODO("Not yet implemented")
    }

    override fun getAllTypes(transaction: Transaction, receiver: (type: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(transaction: Transaction, type: String) {
        TODO("Not yet implemented")
    }

    override fun createStats(
        transaction: Transaction,
        type: String,
        count: Long,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    ) {
        TODO("Not yet implemented")
    }

    override fun getStats(transaction: Transaction, type: String): ItemListStats? {
        TODO("Not yet implemented")
    }

    override fun getStatsCount(transaction: Transaction, type: String): Long {
        TODO("Not yet implemented")
    }

    override fun incrementStatsCount(transaction: Transaction, type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun decrementStatsCount(transaction: Transaction, type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsCount(transaction: Transaction, type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsFirstItem(transaction: Transaction, type: String, id: String) {
        TODO("Not yet implemented")
    }

    override fun updateStatsLastItem(transaction: Transaction, type: String, id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteStats(transaction: Transaction, type: String) {
        TODO("Not yet implemented")
    }
}
