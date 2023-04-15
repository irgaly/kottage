package io.github.irgaly.kottage.platform

expect object Platform {
    val isJs: Boolean
    val isAndroid: Boolean
    val isIos: Boolean
    val isJvm: Boolean
    val isJvmLinux: Boolean
    val isJvmMacos: Boolean
    val isJvmWindows: Boolean
    val isNodejs: Boolean
    val isNodejsLinux: Boolean
    val isNodejsMacos: Boolean
    val isNodejsWindows: Boolean
    val isBrowser: Boolean
    val isLinux: Boolean
    val isWindows: Boolean
    val isMacos: Boolean
}
