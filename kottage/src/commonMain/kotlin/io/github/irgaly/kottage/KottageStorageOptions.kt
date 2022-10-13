package io.github.irgaly.kottage

import io.github.irgaly.kottage.encoder.KottageEncoder
import io.github.irgaly.kottage.property.KottageStore
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
    val ignoreJsonDeserializationError: Boolean,
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
        /**
         * ignore JsonSerializationException when reading value
         *
         * This option affects:
         * * [KottageStorage.get]
         * * [KottageStorage.getOrNull]
         * * [KottageStore.read]
         *
         * This option does not affects:
         * * [KottageStorage.exists]
         * * [KottageEntry] operations
         * * [KottageList] operations
         *
         * default: false
         */
        var ignoreJsonDeserializationError: Boolean = false,
        var json: Json? = null,
        var encoder: KottageEncoder? = null
    ) {
        fun build(): KottageStorageOptions {
            return KottageStorageOptions(
                strategy = strategy,
                defaultExpireTime = defaultExpireTime,
                maxEventEntryCount = maxEventEntryCount,
                eventExpireTime = eventExpireTime,
                ignoreJsonDeserializationError = ignoreJsonDeserializationError,
                json = json,
                encoder = encoder
            )
        }
    }
}
