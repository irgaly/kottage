package io.github.irgaly.kottage

import kotlinx.serialization.json.Json
import io.github.irgaly.kottage.strategy.KkvsFifoStrategy
import io.github.irgaly.kottage.strategy.KkvsKvsStrategy
import io.github.irgaly.kottage.strategy.KkvsStrategy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Storage Options
 */
@OptIn(ExperimentalTime::class)
data class KkvsStorageOptions (
    val strategy: KkvsStrategy,
    val defaultExpireTime: Duration?,
    val autoClean: Boolean,
    val json: Json?
) {
    data class Builder(
        var strategy: KkvsStrategy,
        var defaultExpireTime: Duration?,
        var autoClean: Boolean,
        var json: Json? = null
    ) {
        fun build(): KkvsStorageOptions {
            return KkvsStorageOptions(
                strategy = strategy,
                defaultExpireTime = defaultExpireTime,
                autoClean = autoClean,
                json = json
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
fun kkvsStorage(builder: KkvsStorageOptions.Builder.() -> Unit): KkvsStorageOptions {
    return KkvsStorageOptions.Builder(
        strategy = KkvsKvsStrategy(),
        defaultExpireTime = null,
        autoClean = false
    ).apply(builder).build()
}

@OptIn(ExperimentalTime::class)
fun kkvsCache(builder: KkvsStorageOptions.Builder.() -> Unit): KkvsStorageOptions {
    return KkvsStorageOptions.Builder(
        strategy = KkvsFifoStrategy(1000),
        defaultExpireTime = 30.days,
        autoClean = true
    ).apply(builder).build()
}
