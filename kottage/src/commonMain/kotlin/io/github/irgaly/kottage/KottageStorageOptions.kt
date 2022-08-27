package io.github.irgaly.kottage

import io.github.irgaly.kottage.strategy.KottageFifoStrategy
import io.github.irgaly.kottage.strategy.KottageKvsStrategy
import io.github.irgaly.kottage.strategy.KottageStrategy
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Storage Options
 */
@OptIn(ExperimentalTime::class)
data class KottageStorageOptions(
    val strategy: KottageStrategy,
    val defaultExpireTime: Duration?,
    val autoClean: Boolean,
    val json: Json?
) {
    data class Builder(
        var strategy: KottageStrategy,
        var defaultExpireTime: Duration?,
        var autoClean: Boolean,
        var json: Json? = null
    ) {
        fun build(): KottageStorageOptions {
            return KottageStorageOptions(
                strategy = strategy,
                defaultExpireTime = defaultExpireTime,
                autoClean = autoClean,
                json = json
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
fun kottageStorage(builder: KottageStorageOptions.Builder.() -> Unit): KottageStorageOptions {
    return KottageStorageOptions.Builder(
        strategy = KottageKvsStrategy(),
        defaultExpireTime = null,
        autoClean = false
    ).apply(builder).build()
}

@OptIn(ExperimentalTime::class)
fun kottageCache(builder: KottageStorageOptions.Builder.() -> Unit): KottageStorageOptions {
    return KottageStorageOptions.Builder(
        strategy = KottageFifoStrategy(1000),
        defaultExpireTime = 30.days,
        autoClean = true
    ).apply(builder).build()
}
