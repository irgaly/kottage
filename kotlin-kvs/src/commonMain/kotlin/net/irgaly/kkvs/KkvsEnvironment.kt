package net.irgaly.kkvs

import net.irgaly.kkvs.platform.Context
import net.irgaly.kkvs.platform.KkvsPlatformCalendar

/**
 * Runtime Environment
 */
expect class KkvsEnvironment {
    val context: Context
    val calendar: KkvsPlatformCalendar
}
