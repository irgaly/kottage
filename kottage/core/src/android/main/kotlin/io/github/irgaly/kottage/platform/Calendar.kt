package io.github.irgaly.kottage.platform

actual class Calendar {
    actual companion object {
        actual fun getUnixTimeMillis(): Long {
            return System.currentTimeMillis()
        }
    }
}
