package io.github.irgaly.kottage.platform

import korlibs.time.DateTime
import korlibs.time.DateTimeTz

data class TestCalendar(
    var now: DateTimeTz
) : KottageCalendar {
    override fun nowUnixTimeMillis(): Long {
        return now.utc.unixMillisLong
    }

    fun setUtc(date: DateTime) {
        now = date.utc
    }
}
