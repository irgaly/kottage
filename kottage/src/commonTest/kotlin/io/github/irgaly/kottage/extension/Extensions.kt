package io.github.irgaly.kottage.extension

import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.KottageLogger
import io.github.irgaly.kottage.platform.TestCalendar
import korlibs.time.DateTime
import korlibs.time.milliseconds
import kotlinx.coroutines.CoroutineScope

fun buildKottage(
    name: String,
    directory: String,
    scope: CoroutineScope,
    context: KottageContext,
    builder: (KottageOptions.Builder.() -> Unit)? = null
): Pair<Kottage, TestCalendar> {
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc - 1.milliseconds)
    val environment = KottageEnvironment(
        context,
        calendar,
        object : KottageLogger {
            override suspend fun debug(message: String) {
                println("KottageLogger DEBUG: $message")
            }

            override suspend fun error(message: String) {
                println("KottageLogger ERROR: $message")
            }
        }
    )
    val kottage = Kottage(name, directory, environment, scope, optionsBuilder = builder)
    // 初回の Event Flow が流れるようにするために、インスタンス生成後に時間を進める
    calendar.now += 1.milliseconds
    return Pair(kottage, calendar)
}
