package io.github.irgaly.kottage.sample

import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.put
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.js.Date

fun main() {
    window.onload = {
        MainScope().launch {
            val calendar = object: KottageCalendar {
                override fun nowUnixTimeMillis(): Long {
                    return Date.now().toLong()
                }
            }
            val environment = KottageEnvironment(KottageContext(), calendar)
            val kottage = Kottage("name", "directory", environment, this) {

            }
            val storage = kottage.storage("storage")
            storage.put("key1", "value1")
        }
    }
}
