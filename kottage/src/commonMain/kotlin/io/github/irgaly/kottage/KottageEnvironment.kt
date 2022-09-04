package io.github.irgaly.kottage

import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.platform.KottageContext

/**
 * Runtime Environment
 */
data class KottageEnvironment(
    val context: KottageContext,
    val calendar: KottageCalendar
)
