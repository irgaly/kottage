package io.github.irgaly.test.platform

private val fs: dynamic get() = js("require('fs')")
private val path: dynamic get() = js("require('path')")
private val os: dynamic get() = js("require('os')")

actual class Files {
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            return if (isBrowser()) {
                "js-browser-dummy-temporary-directory"
            } else {
                fs.mkdtempSync(path.join(os.tmpdir(), "")).unsafeCast<String>()
            }
        }

        actual fun deleteRecursively(directoryPath: String): Boolean {
            if (isNodejs()) {
                fs.rmSync(directoryPath, js("{recursive: true, force: true}"))
            }
            return true
        }
    }
}

