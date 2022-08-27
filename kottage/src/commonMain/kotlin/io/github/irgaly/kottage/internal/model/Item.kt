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
            return "$type+$key"
        }

        fun fromEntityKey(entityKey: String, type: String): String {
            return entityKey.removePrefix("$type+")
        }
    }

    fun getEntityKey(): String {
        return toEntityKey(key, type)
    }

    fun isAvailable(now: Long): Boolean {
        return expireAt?.let { now < it } ?: true
    }

    fun isExpired(now: Long): Boolean {
        return expireAt?.let { it <= now } ?: false
    }
}
