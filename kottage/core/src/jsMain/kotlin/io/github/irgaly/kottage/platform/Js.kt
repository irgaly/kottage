package io.github.irgaly.kottage.platform

@Suppress("unused")
fun isBrowser(): Boolean {
    return (js("typeof window") != "undefined")
}

@Suppress("unused")
fun isNodejs(): Boolean {
    return (js("typeof window") == "undefined")
}

@Suppress("unused")
internal fun isLinux(): Boolean {
    return if (isBrowser()) {
        false
    } else {
        (js("require('os').platform()") == "linux")
    }
}

@Suppress("unused")
internal fun isMacos(): Boolean {
    return if (isBrowser()) {
        false
    } else {
        (js("require('os').platform()") == "darwin")
    }
}

@Suppress("unused")
internal fun isWindows(): Boolean {
    return if (isBrowser()) {
        false
    } else {
        (js("require('os').platform()") == "win32")
    }
}
