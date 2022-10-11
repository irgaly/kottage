package io.github.irgaly.kottage.internal.model

internal data class ItemListEntry(
    val id: String,
    val type: String,
    val itemType: String,
    val itemKey: String?,
    val previousId: String?,
    val nextId: String?,
    val expireAt: Long?,
    val userInfo: String?,
    val userPreviousKey: String?,
    val userCurrentKey: String?,
    val userNextKey: String?
) {
    val itemExists: Boolean get() = (itemKey != null)
    val isFirst: Boolean get() = (previousId == null)
    val isLast: Boolean get() = (nextId == null)

    fun isExpired(time: Long): Boolean {
        return expireAt?.let { it <= time } ?: false
    }
}
