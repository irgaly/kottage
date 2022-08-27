package io.github.irgaly.kottage.platform

expect class Files {
    companion object {
        fun exists(path: String): Boolean
        fun mkdirs(directoryPath: String): Boolean
    }
}
