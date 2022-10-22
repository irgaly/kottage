package io.github.irgaly.kottage

import com.soywiz.klock.DateTime
import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.matchers.shouldBe

class KottageMigrationTest : KottageSpec("migration", body = {
    describe("Database Migration") {
        context("from 1") {
            val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
            val environment = KottageEnvironment(KottageContext(), calendar)
            it("to 2") {
                Kottage.createOldDatabase(
                    "test",
                    tempDirectory,
                    environment,
                    1
                )
                val kottage = Kottage("test", tempDirectory, environment)
                val cache = kottage.cache("cache1")
                cache.put("key2", "value2")
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
            }
        }
    }
})
