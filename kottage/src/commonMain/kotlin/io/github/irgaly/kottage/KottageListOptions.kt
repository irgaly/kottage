package io.github.irgaly.kottage

import kotlin.time.Duration

/**
 * List Options
 */
data class KottageListOptions(
    val expireTime: Duration?
) {
    data class Builder(
        var expireTime: Duration?
    ) {
        fun build(): KottageListOptions {
            return KottageListOptions(
                expireTime = expireTime
            )
        }
    }
}
