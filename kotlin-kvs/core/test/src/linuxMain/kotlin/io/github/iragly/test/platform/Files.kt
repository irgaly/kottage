package io.github.iragly.test.platform

actual class Files {
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            throw NotImplementedError()
        }
    }
}
