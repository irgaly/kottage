package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Kotest Config
 */
@Suppress("unused")
class ProjectConfig : AbstractProjectConfig() {
    override val timeout: Duration = 10.minutes
    override val invocationTimeout: Duration = 30.seconds
}