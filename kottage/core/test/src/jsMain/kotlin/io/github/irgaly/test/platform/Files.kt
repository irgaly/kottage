package io.github.irgaly.test.platform

actual class Files {
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            return "js-dummy-temporary-directory"
        }

        actual fun deleteRecursively(directoryPath: String): Boolean {
            return true
        }
    }
}
