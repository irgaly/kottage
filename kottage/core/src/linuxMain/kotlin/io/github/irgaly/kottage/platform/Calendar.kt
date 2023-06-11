
package io.github.irgaly.kottage.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.CLOCK_REALTIME_COARSE
import platform.posix.clock_gettime
import platform.posix.timespec

@OptIn(ExperimentalForeignApi::class)
actual class Calendar {
    actual companion object {
        @OptIn(ExperimentalForeignApi::class)
        actual fun getUnixTimeMillis(): Long {
            return memScoped {
                val spec = alloc<timespec>()
                clock_gettime(CLOCK_REALTIME_COARSE, spec.ptr)
                (spec.tv_sec * 1000L) + (spec.tv_nsec / 1000_000L)
            }
        }
    }
}
