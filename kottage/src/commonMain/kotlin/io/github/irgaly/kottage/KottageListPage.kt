package io.github.irgaly.kottage

data class KottageListPage<T: Any>(
    val items: List<KottageListItem<T>>,
    val previousPositionId: String?,
    val nextPositionId: String?
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val hasPrevious: Boolean get() = (previousPositionId != null)
    val hasNext: Boolean get() = (nextPositionId != null)
}
