package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemListEntry
import kotlin.reflect.KType

data class KottageListItem<T : Any>(
    val positionId: String,
    val previousPositionId: String?,
    val nextPositionId: String?,
    val itemKey: String,
    val entry: KottageEntry<T>,
    val previousKey: String?,
    val currentKey: String?,
    val nextKey: String?
) {
    companion object {
        internal fun <T : Any> from(
            entry: ItemListEntry,
            itemKey: String,
            item: Item,
            type: KType,
            encoder: Encoder
        ): KottageListItem<T> {
            return KottageListItem(
                positionId = entry.id,
                previousPositionId = entry.previousId,
                nextPositionId = entry.nextId,
                itemKey = itemKey,
                entry = KottageEntry(item, type, encoder),
                previousKey = entry.userPreviousKey,
                currentKey = entry.userCurrentKey,
                nextKey = entry.userNextKey
            )
        }
    }
}
