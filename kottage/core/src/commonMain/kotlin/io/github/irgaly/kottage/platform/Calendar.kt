package io.github.irgaly.kottage.platform

expect class Calendar {
    companion object {
        fun getUnixTimeMillis(): Long
    }
}
