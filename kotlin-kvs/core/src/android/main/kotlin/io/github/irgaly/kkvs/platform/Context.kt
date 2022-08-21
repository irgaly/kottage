package io.github.irgaly.kkvs.platform

actual class Context {
    lateinit var context: android.content.Context
        private set

    actual constructor() {
        throw NotImplementedError("use context constructor on Android platform")
    }

    constructor(context: android.content.Context) {
        this.context = context
    }
}
