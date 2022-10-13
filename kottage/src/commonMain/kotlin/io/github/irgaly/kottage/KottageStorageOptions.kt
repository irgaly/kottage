package io.github.irgaly.kottage

import io.github.irgaly.kottage.encoder.KottageEncoder
import io.github.irgaly.kottage.strategy.KottageStrategy
import kotlinx.serialization.json.Json
import kotlin.time.Duration

/**
 * Storage Options
 */
data class KottageStorageOptions(
    val strategy: KottageStrategy,
    val defaultExpireTime: Duration?,
    val maxEventEntryCount: Long,
    val eventExpireTime: Duration?,
    val json: Json?,
    val encoder: KottageEncoder?
) {
    data class Builder(
        var strategy: KottageStrategy,
        var defaultExpireTime: Duration?,
        /**
         * Upper limit of saved event entries count.
         * Automatically delete event entries when saved entries count is over the limit.
         * Deletion count = (maxEventEntryCount * 0.25)
         */
        var maxEventEntryCount: Long,
        var eventExpireTime: Duration?,
        var json: Json? = null,
        var encoder: KottageEncoder? = null
    ) {
        fun build(): KottageStorageOptions {
            return KottageStorageOptions(
                strategy = strategy,
                defaultExpireTime = defaultExpireTime,
                maxEventEntryCount = maxEventEntryCount,
                eventExpireTime = eventExpireTime,
                json = json,
                encoder = encoder
            )
        }
    }
}
