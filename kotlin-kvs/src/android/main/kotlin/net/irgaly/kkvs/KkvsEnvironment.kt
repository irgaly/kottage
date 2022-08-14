package net.irgaly.kkvs

import android.content.Context
import net.irgaly.kkvs.platform.KkvsPlatformCalendar

actual class KkvsEnvironment(
    val context: Context,
    actual val calendar: KkvsPlatformCalendar
)
