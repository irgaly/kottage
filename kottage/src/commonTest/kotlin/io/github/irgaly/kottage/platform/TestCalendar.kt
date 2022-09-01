package io.github.irgaly.kottage.platform

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz

data class TestCalendar(
    var now: DateTimeTz
) : KottageCalendar {
    override fun nowUtcEpochTimeMillis(): Long {
        return now.utc.unixMillisLong
    }

    fun setUtc(date: DateTime) {
        now = date.utc
    }
}
