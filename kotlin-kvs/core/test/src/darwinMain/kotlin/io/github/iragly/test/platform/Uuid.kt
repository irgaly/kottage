package io.github.iragly.test.platform

import platform.Foundation.NSUUID


actual class Uuid {
    actual companion object {
        actual fun generateUuid(): String {
            // https://developer.apple.com/documentation/foundation/nsuuid
            // UUIDv4
            // 68753A44-4D6F-1226-9C60-0050E4C00067
            return NSUUID().UUIDString
        }
    }
}
