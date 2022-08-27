package io.github.irgaly.kottage

import io.github.irgaly.kottage.platform.Context
import io.github.irgaly.kottage.platform.KkvsPlatformCalendar

/**
 * Runtime Environment
 */
data class KkvsEnvironment(
    val context: Context,
    val calendar: KkvsPlatformCalendar
)
