package io.github.irgaly.test.platform

expect class Files {
    companion object {
        fun createTemporaryDirectory(): String
        fun deleteRecursively(directoryPath: String): Boolean
    }
}
