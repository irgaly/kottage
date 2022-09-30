package io.github.irgaly.kottage.internal.model

data class ItemListStats(
    val listType: String,
    val count: Long,
    val firstItemPositionId: String,
    val lastItemPositionId: String,
)
