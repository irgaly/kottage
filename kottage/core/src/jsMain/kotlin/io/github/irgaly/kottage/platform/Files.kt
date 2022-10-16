package io.github.irgaly.kottage.platform

actual class Files {
    actual companion object {
        actual fun exists(path: String): Boolean {
            throw UnsupportedOperationException("browser js cannot access to File Storage")
        }

        actual fun mkdirs(directoryPath: String): Boolean {
            throw UnsupportedOperationException("browser js cannot access to File Storage")
        }

        actual val separator: String = "/"
    }
}
