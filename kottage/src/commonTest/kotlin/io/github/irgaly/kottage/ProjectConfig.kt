package io.github.irgaly.kottage

import io.kotest.core.config.AbstractProjectConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Kotest Config
 */
@Suppress("unused")
object ProjectConfig: AbstractProjectConfig() {
    override val timeout: Duration = 10.minutes
    override val invocationTimeout: Long = 30.seconds.inWholeMilliseconds
}
