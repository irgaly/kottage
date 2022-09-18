package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.model.ItemEventType

enum class KottageEventType {
    Create,
    Update,
    Delete;

    companion object {
        internal fun from(itemEventType: ItemEventType): KottageEventType {
            return when (itemEventType) {
                ItemEventType.Create -> Create
                ItemEventType.Update -> Update
                ItemEventType.Delete -> Delete
            }
        }
    }
}
