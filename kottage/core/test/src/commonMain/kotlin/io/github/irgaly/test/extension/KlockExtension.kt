package io.github.irgaly.test.extension

import korlibs.time.TimeSpan
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val TimeSpan.duration: Duration get() = this.millisecondsLong.milliseconds
