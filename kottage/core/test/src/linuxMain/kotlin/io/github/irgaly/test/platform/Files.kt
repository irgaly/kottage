package io.github.irgaly.test.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import platform.posix.FTW_DEPTH
import platform.posix.FTW_PHYS
import platform.posix.getenv
import platform.posix.mkdtemp
import platform.posix.nftw
import platform.posix.remove

@OptIn(ExperimentalForeignApi::class)
actual class Files {
    @OptIn(ExperimentalForeignApi::class)
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            val tempDirectory =
                sequenceOf("TMPDIR", "TMP", "TEMP", "TEMPDIR").firstNotNullOfOrNull {
                    getenv(it)?.toKString()
                } ?: "/tmp"
            val directory = mkdtemp("$tempDirectory/tmpdir.XXXXXX".cstr)?.toKString()
            return checkNotNull(directory)
        }


        actual fun deleteRecursively(directoryPath: String): Boolean {
            val ret = nftw(
                directoryPath,
                staticCFunction { pathName, _, _, _ ->
                    remove(pathName!!.toKString())
                },
                64,
                FTW_DEPTH or FTW_PHYS
            )
            return (ret != -1)
        }
    }
}
