package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction

internal class KottageIndexeddbStatsRepository : KottageStatsRepository {
    override suspend fun getLastEvictAt(transaction: Transaction): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateLastEvictAt(transaction: Transaction, now: Long) {
        TODO("Not yet implemented")
    }
}
