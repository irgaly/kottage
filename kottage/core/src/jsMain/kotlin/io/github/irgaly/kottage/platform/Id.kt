package io.github.irgaly.kottage.platform

actual class Id {
    actual companion object {
        actual fun generateUuidV4(): String {
            throw NotImplementedError()
        }
        actual fun generateUuidV4Short(): String {
            throw NotImplementedError()
        }
    }
}
