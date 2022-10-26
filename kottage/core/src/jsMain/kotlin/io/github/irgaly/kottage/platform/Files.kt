package io.github.irgaly.kottage.platform

actual class Files {
    actual companion object {
        actual fun exists(path: String): Boolean {
            // js dummy implementation
            return true
        }

        actual fun mkdirs(directoryPath: String): Boolean {
            // js dummy implementation
            return true
        }

        actual val separator: String = "/"
    }
}
