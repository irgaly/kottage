package io.github.irgaly.kottage

import io.github.irgaly.kottage.platform.KottageContext
import io.github.irgaly.kottage.platform.TestCalendar
import io.github.irgaly.kottage.test.KottageSpec
import io.kotest.matchers.shouldBe
import korlibs.time.DateTime

class KottageMigrationTest : KottageSpec("migration", body = {
    describe("Database Migration") {
        context("to latest") {
            val calendar = TestCalendar(DateTime(2022, 1, 1).utc)
            val environment = KottageEnvironment(KottageContext(), calendar)
            it("from 1") {
                Kottage.createOldDatabase("v1", tempDirectory, environment, 1)
                val kottage = Kottage("v1", tempDirectory, environment, specScope)
                val cache = kottage.cache("cache1")
                cache.put("key2", "value2")
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
            }
            it("from 2") {
                Kottage.createOldDatabase("v2", tempDirectory, environment, 2)
                val kottage = Kottage("v2", tempDirectory, environment, specScope)
                val cache = kottage.cache("cache1")
                cache.put("key2", "value2")
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
            }
            it("from 3") {
                Kottage.createOldDatabase("v3", tempDirectory, environment, 3)
                val kottage = Kottage("v2", tempDirectory, environment, specScope)
                val cache = kottage.cache("cache1")
                cache.put("key2", "value2")
                cache.get<String>("key1") shouldBe "value1"
                cache.get<String>("key2") shouldBe "value2"
            }
        }
    }
})
