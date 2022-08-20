package io.github.iragly.test.platform

expect class Files {
    companion object {
        fun createTemporaryDirectory(): String
    }
}
