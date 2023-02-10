package io.github.irgaly.kottage.sample

import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.get
import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.put
import kotlinx.coroutines.coroutineScope
import kotlin.js.Date

suspend fun main() {
    coroutineScope {
        val fs = js("require('fs')")
        val path = js("require('path')")
        val os = js("require('os')")
        val tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "")).unsafeCast<String>()
        console.log("tempDir = $tempDir")
        val calendar = object : KottageCalendar {
            override fun nowUnixTimeMillis(): Long {
                return Date.now().toLong()
            }
        }
        val environment = KottageEnvironment(KottageContext(), calendar)
        val kottage = Kottage("name", "directory", environment, this) {
        }
        val storage = kottage.storage("storage")
        storage.put("key1", "value1")
        val result = storage.get<String>("key1")
        console.log("result = $result")
    }
}
