package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.data.sqlite.KottageDatabase

internal class KottageSqliteStatsRepository(
    private val database: KottageDatabase
) : KottageStatsRepository {
    private val key = "kottage"

    override fun getLastEvictAt(): Long {
        database.statsQueries
            .insertIfNotExists(key)
        return database.statsQueries
            .selectLastEvictAt(key)
            .executeAsOne()
    }

    override fun updateLastEvictAt(now: Long) {
        database.statsQueries
            .insertIfNotExists(key)
        database.statsQueries
            .updateLastEvictAt(now, key)
    }
}
