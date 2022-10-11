package io.github.irgaly.kottage.extension

import com.soywiz.klock.DateTime
import com.soywiz.klock.milliseconds
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.TestCalendar

fun buildKottage(
    name: String, directory: String, builder: (KottageOptions.Builder.() -> Unit)?
): Pair<Kottage, TestCalendar> {
    val calendar = TestCalendar(DateTime(2022, 1, 1).utc - 1.milliseconds)
    val environment = KottageEnvironment(KottageContext(), calendar)
    val kottage = Kottage(name, directory, environment, optionsBuilder = builder)
    // 初回の Event Flow が流れるようにするために、インスタンス生成後に時間を進める
    calendar.now += 1.milliseconds
    return Pair(kottage, calendar)
}
