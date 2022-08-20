package io.github.iragly.test.platform

expect class Uuid {
    companion object {
        fun generateUuid(): String
    }
}
