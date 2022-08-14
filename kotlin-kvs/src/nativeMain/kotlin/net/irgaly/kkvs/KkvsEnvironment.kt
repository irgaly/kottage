package net.irgaly.kkvs

import net.irgaly.kkvs.platform.Context
import net.irgaly.kkvs.platform.KkvsPlatformCalendar

actual class KkvsEnvironment(
    actual val calendar: KkvsPlatformCalendar
) {
    actual val context: Context = Context()
}
