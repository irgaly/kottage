package io.github.irgaly.kottage.platform

actual object Platform {
    actual val isJs: Boolean = false
    actual val isAndroid: Boolean = false
    actual val isIos: Boolean = false
    actual val isJvm: Boolean = false
    actual val isJvmLinux: Boolean = false
    actual val isJvmMacos: Boolean = false
    actual val isJvmWindows: Boolean = false
    actual val isNodejs: Boolean = false
    actual val isNodejsLinux: Boolean = false
    actual val isNodejsMacos: Boolean = false
    actual val isNodejsWindows: Boolean = false
    actual val isBrowser: Boolean = false
    actual val isLinux: Boolean = false
    actual val isWindows: Boolean = false
    actual val isMacos: Boolean = true
}
