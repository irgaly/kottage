package io.github.irgaly.kottage.sample

fun main() {
    val fs = js("require('fs')")
    val path = js("require('path')")
    val os = js("require('os')")
    val tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "")).unsafeCast<String>()
    console.log("tempDir = $tempDir")
}
