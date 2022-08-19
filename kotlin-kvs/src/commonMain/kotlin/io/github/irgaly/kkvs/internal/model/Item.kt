package io.github.irgaly.kkvs.internal.model

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
    fun isAvailable(now: Long): Boolean {
        return expireAt?.let { now < it } ?: true
    }

    fun isExpired(now: Long): Boolean {
        return expireAt?.let { it <= now } ?: false
    }
}
