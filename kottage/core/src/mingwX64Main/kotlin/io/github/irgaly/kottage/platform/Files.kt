package io.github.irgaly.kottage.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.ERROR_SUCCESS
import platform.windows.GetFileAttributesW
import platform.windows.INVALID_FILE_ATTRIBUTES
import platform.windows.SHCreateDirectoryExW

@OptIn(ExperimentalForeignApi::class)
actual class Files {
    actual companion object {
        actual fun exists(path: String): Boolean {
            val windowsPath = path.replace("/", separator)
            return (GetFileAttributesW(windowsPath) != INVALID_FILE_ATTRIBUTES)
        }

        actual fun mkdirs(directoryPath: String): Boolean {
            val windowsPath = directoryPath.replace("/", separator)
            return (SHCreateDirectoryExW(null, windowsPath, null) == ERROR_SUCCESS)
        }

        actual val separator: String = "\\"
    }
}
