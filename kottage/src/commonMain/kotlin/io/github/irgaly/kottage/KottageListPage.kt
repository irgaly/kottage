package io.github.irgaly.kottage

@Suppress("unused")
data class KottageListPage(
    val items: List<KottageListEntry>,
    val previousPositionId: String?,
    val nextPositionId: String?,
    /**
     * has previous page
     */
    val hasPrevious: Boolean,
    /**
     * has next page
     */
    val hasNext: Boolean
) {
    val isEmpty: Boolean get() = items.isEmpty()
}
