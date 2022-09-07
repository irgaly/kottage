package io.github.irgaly.kottage.platform

import java.util.*

actual class Id {
    actual companion object {
        actual fun generateUuidV4(): String {
            // https://docs.oracle.com/javase/jp/8/docs/api/java/util/UUID.html
            // UUID v4
            // 93abd516-f6b1-4108-b7af-d416f4b59f5d
            return UUID.randomUUID().toString()
        }

        actual fun generateUuidV4Short(): String {
            return UUID.randomUUID().toString().replace("-", "")
        }
    }
}
