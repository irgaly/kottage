package io.github.irgaly.kottage.platform

import platform.Foundation.NSUUID

actual class Id {
    actual companion object {
        actual fun generateUuidV4(): String {
            // https://developer.apple.com/documentation/foundation/nsuuid
            // UUIDv4
            // 68753A44-4D6F-1226-9C60-0050E4C00067
            return NSUUID().UUIDString
        }

        actual fun generateUuidV4Short(): String {
            return NSUUID().UUIDString.replace("-", "")
        }
    }
}
