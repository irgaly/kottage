package io.github.irgaly.kottage.platform

import kotlin.js.Date


actual class Calendar {
    actual companion object {
        actual fun getUnixTimeMillis(): Long {
            return Date.now().toLong()
        }
    }
}
