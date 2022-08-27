package io.github.irgaly.kkvs.platform

expect class Files {
    companion object {
        fun exists(path: String): Boolean
        fun mkdirs(directoryPath: String): Boolean
    }
}
