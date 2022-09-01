package io.github.irgaly.kottage

import io.github.irgaly.kottage.platform.Context
import io.github.irgaly.kottage.platform.KottageCalendar

/**
 * Runtime Environment
 */
data class KottageEnvironment(
    val context: Context,
    val calendar: KottageCalendar
)
