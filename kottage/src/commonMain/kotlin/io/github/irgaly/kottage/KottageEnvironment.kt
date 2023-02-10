package io.github.irgaly.kottage

import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.KottageLogger

/**
 * Runtime Environment
 */
data class KottageEnvironment(
    val context: KottageContext,
    val calendar: KottageCalendar? = null,
    val logger: KottageLogger? = null
)
