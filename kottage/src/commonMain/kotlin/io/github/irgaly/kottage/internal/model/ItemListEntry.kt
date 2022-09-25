package io.github.irgaly.kottage.internal.model

internal data class ItemListEntry(
    val id: String,
    val type: String,
    val itemType: String,
    val itemKey: String?,
    val previousId: String?,
    val nextId: String?,
    val userPreviousKey: String?,
    val userCurrentKey: String?,
    val userNextKey: String?
)
