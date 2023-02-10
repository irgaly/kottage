package io.github.irgaly.kottage.platform

interface KottageLogger {
    suspend fun debug(message: String) {
        // empty default method
    }
    suspend fun error(message: String) {
        // empty default method
    }
}
