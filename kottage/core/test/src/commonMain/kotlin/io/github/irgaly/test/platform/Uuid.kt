package io.github.irgaly.test.platform

expect class Uuid {
    companion object {
        fun generateUuid(): String
    }
}
