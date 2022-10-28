package io.github.irgaly.kottage.internal.repository

import com.juul.indexeddb.ObjectStore
import com.juul.indexeddb.WriteTransaction
import io.github.irgaly.kottage.data.indexeddb.KottageIndexeddbDatabase
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal class KottageIndexeddbItemListRepository(
    private val database: KottageIndexeddbDatabase
) : KottageItemListRepository {
    override suspend fun upsert(transaction: Transaction, entry: ItemListEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePreviousId(transaction: Transaction, id: String, previousId: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun updateNextId(transaction: Transaction, id: String, nextId: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun updateItemKey(
        transaction: Transaction,
        id: String,
        itemType: String,
        itemKey: String?,
        expireAt: Long?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateExpireAt(transaction: Transaction, id: String, expireAt: Long?) {
        TODO("Not yet implemented")
    }

    override suspend fun removeItemKey(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserData(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun get(transaction: Transaction, id: String): ItemListEntry? {
        TODO("Not yet implemented")
    }

    override suspend fun getIds(transaction: Transaction, itemType: String, itemKey: String): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getInvalidatedItemIds(
        transaction: Transaction,
        type: String,
        beforeExpireAt: Long?,
        limit: Long
    ): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getCount(transaction: Transaction, type: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getInvalidatedItemCount(transaction: Transaction, type: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getAllTypes(transaction: Transaction, receiver: suspend (type: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(transaction: Transaction, type: String) {
        TODO("Not yet implemented")
    }

    override suspend fun createStats(
        transaction: Transaction,
        type: String,
        count: Long,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getStats(transaction: Transaction, type: String): ItemListStats? {
        TODO("Not yet implemented")
    }

    override suspend fun getStatsCount(transaction: Transaction, type: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun incrementStatsCount(transaction: Transaction, type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun decrementStatsCount(transaction: Transaction, type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateStatsCount(transaction: Transaction, type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateStatsFirstItem(transaction: Transaction, type: String, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateStatsLastItem(transaction: Transaction, type: String, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteStats(transaction: Transaction, type: String) {
        TODO("Not yet implemented")
    }

    private inline fun <R> Transaction.store(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("item_list")) }
    }
}
