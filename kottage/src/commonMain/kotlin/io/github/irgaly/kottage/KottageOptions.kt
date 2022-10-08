package io.github.irgaly.kottage

import kotlin.time.Duration

/**
 * Kottage Options
 */
data class KottageOptions(
    val autoCompactionDuration: Duration?,
    val garbageCollectionTimeOfInvalidatedListEntries: Duration?
) {
    data class Builder(
        /**
         * Execute evicting caches and optimizing database file size
         * on each autoCompactionDuration time elapsed
         * if null, autoCompaction is disabled.
         */
        var autoCompactionDuration: Duration?,
        var garbageCollectionTimeOfInvalidatedListEntries: Duration?
    ) {
        fun build(): KottageOptions {
            return KottageOptions(
                autoCompactionDuration = autoCompactionDuration,
                garbageCollectionTimeOfInvalidatedListEntries = garbageCollectionTimeOfInvalidatedListEntries
            )
        }
    }
}
