package io.github.irgaly.kottage.platform

import kotlinx.browser.window

fun isBrowser(): Boolean {
    @Suppress("SENSELESS_COMPARISON")
    return (window != null)
}

fun isNodejs(): Boolean {
    @Suppress("SENSELESS_COMPARISON")
    return (window == null)
}
