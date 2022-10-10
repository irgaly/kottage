package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemListEntry
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("unused")
class KottageListItem internal constructor(
    val positionId: String,
    val previousPositionId: String?,
    val nextPositionId: String?,
    val itemKey: String,
    val info: String?,
    val previousKey: String?,
    val currentKey: String?,
    val nextKey: String?,
    private val item: Item,
    private val encoder: Encoder
) {
    companion object {
        internal fun from(
            entry: ItemListEntry,
            itemKey: String,
            item: Item,
            encoder: Encoder
        ): KottageListItem {
            return KottageListItem(
                positionId = entry.id,
                previousPositionId = entry.previousId,
                nextPositionId = entry.nextId,
                itemKey = itemKey,
                info = entry.userInfo,
                previousKey = entry.userPreviousKey,
                currentKey = entry.userCurrentKey,
                nextKey = entry.userNextKey,
                item = item,
                encoder = encoder
            )
        }
    }

    fun <T : Any> entry(type: KType): KottageEntry<T> {
        return KottageEntry(
            item = item,
            type = type,
            encoder = encoder
        )
    }

    inline fun <reified T : Any> entry(): KottageEntry<T> = entry(typeOf<T>())
}
