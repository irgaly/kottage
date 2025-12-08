package io.github.irgaly.kottage.internal.model

internal data class Item(
    val key: String,
    val type: String,
    val stringValue: String?,
    val longValue: Long?,
    val doubleValue: Double?,
    @Suppress("ArrayInDataClass")
    val bytesValue: ByteArray?,
    val createdAt: Long,
    val lastReadAt: Long,
    val expireAt: Long?
) {
    companion object {
        fun toEntityKey(key: String, type: String): String {
            require(!type.contains("+")) {
                "itemType should not contains \"+\": type = \"$type\""
            }
            return "$type+$key"
        }

        fun keyFromEntityKey(entityKey: String, type: String): String {
            return entityKey.removePrefix("$type+")
        }
    }

    fun getEntityKey(): String {
        return toEntityKey(key, type)
    }

    /**
     * get estimate value bytes in Database
     *
     * @see https://sqlite.org/datatype3.html
     * @see https://sqlite.org/fileformat.html#record_format
     */
    fun getEstimateValueBytes(): Long {
        return when {
            stringValue != null -> {
                // UTF-8 bytes
                stringValue.encodeToByteArray().size.toLong()
            }

            longValue != null -> {
                when (longValue) {
                    // SQLite 0 -> 0 bytes
                    0L -> 0
                    // SQLite 1 -> 0 bytes
                    1L -> 0
                    // Byte -> 1 bytes
                    in (Byte.MIN_VALUE..Byte.MAX_VALUE) -> 1
                    // Short -> 2 bytes
                    in (Short.MIN_VALUE..Short.MAX_VALUE) -> 2
                    // SQLite 3 bytes INTEGER
                    in (-8_388_608..8_388_607) -> 3
                    // Int -> 4 bytes
                    in (Int.MIN_VALUE..Int.MAX_VALUE) -> 4
                    // SQLite 6 bytes INTEGER
                    in (-140_737_488_355_328..140_737_488_355_327) -> 6
                    // Long -> 8 bytes
                    else -> 8
                }
            }

            doubleValue != null -> {
                // 8 bytes (8-byte IEEE floating point number)
                8
            }

            bytesValue != null -> {
                // Byte to bytes
                bytesValue.size.toLong()
            }

            // this is an invalid item, treat as 0 bytes for fallback
            else -> 0
        }
    }

    fun isAvailable(now: Long): Boolean {
        return expireAt?.let { now < it } ?: true
    }

    fun isExpired(now: Long): Boolean {
        return expireAt?.let { it <= now } ?: false
    }
}
