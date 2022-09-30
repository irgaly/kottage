package io.github.irgaly.kottage

import kotlin.time.Duration

/**
 * List Options
 */
data class KottageListOptions(
    val itemExpireTime: Duration?
) {
    data class Builder(
        var itemExpireTime: Duration?
    ) {
        fun build(): KottageListOptions {
            return KottageListOptions(
                itemExpireTime = itemExpireTime
            )
        }
    }
}
