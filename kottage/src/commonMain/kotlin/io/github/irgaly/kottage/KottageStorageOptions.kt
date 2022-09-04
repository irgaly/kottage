package io.github.irgaly.kottage

import io.github.irgaly.kottage.strategy.KottageStrategy
import kotlinx.serialization.json.Json
import kotlin.time.Duration

/**
 * Storage Options
 */
data class KottageStorageOptions(
    val strategy: KottageStrategy,
    val defaultExpireTime: Duration?,
    val json: Json?
) {
    data class Builder(
        var strategy: KottageStrategy,
        var defaultExpireTime: Duration?,
        var json: Json? = null
    ) {
        fun build(): KottageStorageOptions {
            return KottageStorageOptions(
                strategy = strategy,
                defaultExpireTime = defaultExpireTime,
                json = json
            )
        }
    }
}
