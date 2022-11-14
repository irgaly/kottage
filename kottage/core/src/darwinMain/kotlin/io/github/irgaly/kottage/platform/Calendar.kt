package io.github.irgaly.kottage.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual class Calendar {
    actual companion object {
        actual fun getUnixTimeMillis(): Long {
            return (NSDate().timeIntervalSince1970 * 1000).toLong()
        }
    }
}
