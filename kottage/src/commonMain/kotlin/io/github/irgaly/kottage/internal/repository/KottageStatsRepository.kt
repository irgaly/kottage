package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction

/**
 * stats Table
 *
 * Database statistics
 * * lastEvictAt
 */
internal interface KottageStatsRepository {
    /**
     * get last evicting time
     */
    suspend fun getLastEvictAt(transaction: Transaction): Long

    /**
     * set last evicting time
     */
    suspend fun updateLastEvictAt(transaction: Transaction, now: Long)
}
