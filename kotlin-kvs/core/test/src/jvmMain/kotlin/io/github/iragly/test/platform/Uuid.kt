package io.github.iragly.test.platform

import java.util.*


actual class Uuid {
    actual companion object {
        actual fun generateUuid(): String {
            // https://docs.oracle.com/javase/jp/8/docs/api/java/util/UUID.html
            // UUID v4
            // 93abd516-f6b1-4108-b7af-d416f4b59f5d
            return UUID.randomUUID().toString()
        }
    }
}
