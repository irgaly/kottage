package net.irgaly.kkvs

import net.irgaly.kkvs.platform.KkvsPlatformCalendar

/**
 * Runtime Environment
 */
expect class KkvsEnvironment {
    val calendar: KkvsPlatformCalendar
}
