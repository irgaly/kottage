package io.github.iragly.test.platform

import kotlin.io.path.createTempDirectory

actual class Files {
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io.path/create-temp-directory.html
            // https://docs.oracle.com/javase/jp/8/docs/api/java/nio/file/Files.html#createTempDirectory-java.lang.String-java.nio.file.attribute.FileAttribute...-
            // JVM + macOS: /var/folders/.../6089636834939322082
            return createTempDirectory().toString()
        }
    }
}
