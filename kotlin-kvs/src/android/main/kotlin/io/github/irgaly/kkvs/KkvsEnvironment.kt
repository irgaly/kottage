package io.github.irgaly.kkvs

import io.github.irgaly.kkvs.platform.Context
import io.github.irgaly.kkvs.platform.KkvsPlatformCalendar

actual class KkvsEnvironment(
    androidContext: android.content.Context,
    actual val calendar: KkvsPlatformCalendar
) {
    actual val context: Context = Context(androidContext)
}
