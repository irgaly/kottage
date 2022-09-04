package io.github.irgaly.kottage.internal.repository

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
    fun getLastEvictAt(): Long
    /**
     * set last evicting time
     */
    fun updateLastEvictAt(now: Long)
}
