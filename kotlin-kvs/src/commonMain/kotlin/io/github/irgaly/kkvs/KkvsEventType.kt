package io.github.irgaly.kkvs

import io.github.irgaly.kkvs.internal.model.ItemEventType

enum class KkvsEventType {
    Create,
    Update,
    Delete,
    Expired;

    companion object {
        internal fun from(itemEventType: ItemEventType): KkvsEventType {
            return when (itemEventType) {
                ItemEventType.Create -> Create
                ItemEventType.Update -> Update
                ItemEventType.Delete -> Delete
                ItemEventType.Expired -> Expired
            }
        }
    }
}
