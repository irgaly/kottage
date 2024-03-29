package io.github.irgaly.kottage.platform

actual class Context {
    lateinit var context: android.content.Context
        private set

    actual constructor() {
        throw UnsupportedOperationException("use context constructor on Android platform")
    }

    constructor(context: android.content.Context) {
        this.context = context
    }
}
