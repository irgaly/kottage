package io.github.irgaly.kottage.platform

import kotlin.random.Random

actual class Id {
    actual companion object {
        private const val characters = "0123456789abcdef"
        private val rand = Random.Default

        actual fun generateUuidV4(): String {
            // use pseudo UUID on Linux platform
            return "${generateRandomString(8)}-${generateRandomString(4)}-${generateRandomString(4)}-${generateRandomString(4)}-${generateRandomString(12)}"
        }

        actual fun generateUuidV4Short(): String {
            return generateRandomString(32)
        }

        private fun generateRandomString(length: Int): String {
            val builder = StringBuilder()
            (0 until length).forEach { _ ->
                builder.append(characters[rand.nextInt(characters.length)])
            }
            return builder.toString()
        }
    }
}
