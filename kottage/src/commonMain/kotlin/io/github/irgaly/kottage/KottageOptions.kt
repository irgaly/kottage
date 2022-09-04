package io.github.irgaly.kottage

import kotlin.time.Duration

/**
 * Kottage Options
 */
data class KottageOptions(
    val autoCompactionDuration: Duration?
) {
    data class Builder(
        /**
         * Execute evicting caches and optimizing database file size
         * on each autoCompactionDuration time elapsed
         */
        var autoCompactionDuration: Duration?
    ) {
        fun build(): KottageOptions {
            return KottageOptions(
                autoCompactionDuration = autoCompactionDuration
            )
        }
    }
}
