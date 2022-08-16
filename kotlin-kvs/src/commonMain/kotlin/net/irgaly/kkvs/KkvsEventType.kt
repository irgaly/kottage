package net.irgaly.kkvs

import net.irgaly.kkvs.internal.model.ItemEventType

enum class KkvsEventType {
    Create,
    Update,
    Delete,
    Expired;

    companion object {
        fun from(itemEventType: ItemEventType): KkvsEventType {
            return when(itemEventType) {
                ItemEventType.Create -> Create
                ItemEventType.Update -> Update
                ItemEventType.Delete -> Delete
                ItemEventType.Expired -> Expired
            }
        }
    }
}
