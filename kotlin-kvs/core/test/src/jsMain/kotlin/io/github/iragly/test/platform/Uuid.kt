package io.github.iragly.test.platform


actual class Uuid {
    actual companion object {
        actual fun generateUuid(): String {
            throw NotImplementedError()
        }
    }
}
