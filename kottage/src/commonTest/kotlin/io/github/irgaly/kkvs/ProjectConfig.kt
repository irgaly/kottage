package io.github.irgaly.kkvs

import io.kotest.core.config.AbstractProjectConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Kotest Config
 */
object ProjectConfig: AbstractProjectConfig() {
    override val timeout: Duration = 10.minutes
    override val invocationTimeout: Long = 10.seconds.inWholeMilliseconds
}
