package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.internal.model.ItemEventType

fun ItemEventType.toEntity(): net.irgaly.kkvs.data.sqlite.entity.ItemEventType {
    return when(this) {
        ItemEventType.Create ->  net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Create
        ItemEventType.Update ->  net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Update
        ItemEventType.Delete ->  net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Delete
    }
}
