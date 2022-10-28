package io.github.irgaly.kottage.internal.repository

import com.juul.indexeddb.Key
import com.juul.indexeddb.ObjectStore
import com.juul.indexeddb.WriteTransaction
import io.github.irgaly.kottage.data.indexeddb.KottageIndexeddbDatabase
import io.github.irgaly.kottage.data.indexeddb.extension.jso
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Stats
import io.github.irgaly.kottage.internal.database.Transaction

internal class KottageIndexeddbStatsRepository(
    private val database: KottageIndexeddbDatabase
) : KottageStatsRepository {
    private val key = "kottage"

    override suspend fun getLastEvictAt(transaction: Transaction): Long {
        return transaction.store { store ->
            getOrCreate(store, key).last_evict_at
        }
    }

    override suspend fun updateLastEvictAt(transaction: Transaction, now: Long) {
        transaction.store { store ->
            val stats = getOrCreate(store, key)
            stats.last_evict_at = now
            store.put(stats)
        }
    }

    private suspend fun WriteTransaction.getOrCreate(store: ObjectStore, key: String): Stats {
        var stats = store.get(Key(key))?.unsafeCast<Stats>()
        if (stats == null) {
            stats = jso {
                this.key = key
                last_evict_at = 0L
            }
            store.add(stats)
        }
        return stats
    }

    private inline fun <R> Transaction.store(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("stats")) }
    }
}
