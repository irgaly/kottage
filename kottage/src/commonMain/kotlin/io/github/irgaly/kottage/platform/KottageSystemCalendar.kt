package io.github.irgaly.kottage.platform

/**
 * Default implementation of KottageCalendar
 *
 * use platform specific Unix Time
 */
class KottageSystemCalendar: KottageCalendar {
    override fun nowUnixTimeMillis(): Long {
        return Calendar.getUnixTimeMillis()
    }
}
