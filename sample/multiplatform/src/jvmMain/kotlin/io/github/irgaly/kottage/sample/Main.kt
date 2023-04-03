package io.github.irgaly.kottage.sample

import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.put
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun main() {
    MainScope().launch {
        val environment = KottageEnvironment(KottageContext())
        val kottage = Kottage("name", "directory", environment, this) {

        }
        val storage = kottage.storage("storage")
        storage.put("key1", "value1")
    }
}
