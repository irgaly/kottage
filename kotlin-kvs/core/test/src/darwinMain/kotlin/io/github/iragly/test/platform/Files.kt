package io.github.iragly.test.platform

import kotlinx.cinterop.*
import platform.Foundation.*

actual class Files {
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            return memScoped {
                // https://developer.apple.com/documentation/foundation/1409211-nstemporarydirectory
                // iOS: NSTemporaryDirectory() = (Application Sandbox)/tmp
                // macOS: NSTemporaryDirectory() = /var/folders/...
                // https://developer.apple.com/documentation/foundation/filemanager/1407693-url
                val manager = NSFileManager.defaultManager
                val errorPtr = alloc<ObjCObjectVar<NSError?>>().ptr
                val result = manager.URLForDirectory(
                    directory = NSItemReplacementDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = manager.temporaryDirectory,
                    create = true,
                    error = errorPtr
                )?.absoluteString
                val error = errorPtr.pointed.value
                if (error != null) {
                    throw Exception(error.toString())
                }
                checkNotNull(result)
            }
        }
    }
}
