package io.github.irgaly.kottage

@Suppress("unused")
data class KottageListPage(
    val items: List<KottageListEntry>,
    val previousPositionId: String?,
    val nextPositionId: String?
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val hasPrevious: Boolean get() = (previousPositionId != null)
    val hasNext: Boolean get() = (nextPositionId != null)
}
