package io.github.irgaly.kottage

data class KottageListItem<T: Any> (
    val positionId: String,
    val previousPositionId: String?,
    val nextPositionId: String?,
    val itemKey: String,
    val entry: KottageEntry<T>,
    val previousKey: String?,
    val currentKey: String?,
    val nextKey: String?
)
