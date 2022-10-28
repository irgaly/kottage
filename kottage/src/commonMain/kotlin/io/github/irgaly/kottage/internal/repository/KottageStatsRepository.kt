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
    fun getLastEvictAt(transaction: Transaction): Long

    /**
     * set last evicting time
     */
    fun updateLastEvictAt(transaction: Transaction, now: Long)
}
