package net.irgaly.kkvs

import net.irgaly.kkvs.platform.KkvsPlatformCalendar

actual class KkvsEnvironment(
    actual val calendar: KkvsPlatformCalendar
)
