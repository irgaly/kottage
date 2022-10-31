package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.data.sqlite.KottageDatabase
import io.github.irgaly.kottage.internal.database.Transaction

internal class KottageSqliteStatsRepository(
    private val database: KottageDatabase
) : KottageStatsRepository {
    private val key = "kottage"

    override suspend fun getLastEvictAt(transaction: Transaction): Long {
        database.statsQueries
            .insertIfNotExists(key)
        return database.statsQueries
            .selectLastEvictAt(key)
            .executeAsOne()
    }

    override suspend fun updateLastEvictAt(transaction: Transaction, now: Long) {
        database.statsQueries
            .insertIfNotExists(key)
        database.statsQueries
            .updateLastEvictAt(now, key)
    }
}
