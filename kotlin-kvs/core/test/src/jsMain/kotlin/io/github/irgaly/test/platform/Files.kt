package io.github.irgaly.test.platform

actual class Files {
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            throw NotImplementedError()
        }

        actual fun deleteRecursively(directoryPath: String): Boolean {
            throw NotImplementedError()
        }
    }
}
