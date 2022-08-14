package net.irgaly.kkvs

import net.irgaly.kkvs.platform.Context
import net.irgaly.kkvs.platform.KkvsPlatformCalendar

actual class KkvsEnvironment(
    androidContext: android.content.Context,
    actual val calendar: KkvsPlatformCalendar
) {
    actual val context: Context = Context(androidContext)
}
