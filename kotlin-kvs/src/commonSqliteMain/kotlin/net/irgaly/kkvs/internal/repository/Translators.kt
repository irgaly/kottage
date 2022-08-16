package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.internal.model.Item
import net.irgaly.kkvs.internal.model.ItemEvent
import net.irgaly.kkvs.internal.model.ItemEventType

internal fun net.irgaly.kkvs.data.sqlite.Item.toDomain(): Item {
    return Item(
        key = key,
        type = type,
        stringValue = string_value,
        longValue = long_value,
        doubleValue = double_value,
        bytesValue = bytes_value,
        createdAt = created_at,
        lastReadAt = last_read_at,
        expireAt = expire_at
    )
}

internal fun Item.toEntity(): net.irgaly.kkvs.data.sqlite.Item {
    return net.irgaly.kkvs.data.sqlite.Item(
        key = key,
        type = type,
        string_value = stringValue,
        long_value = longValue,
        double_value = doubleValue,
        bytes_value = bytesValue,
        created_at = createdAt,
        last_read_at = lastReadAt,
        expire_at = expireAt
    )
}

internal fun net.irgaly.kkvs.data.sqlite.Item_event.toDomain(): ItemEvent {
    return ItemEvent(
        createdAt = created_at,
        itemType = item_type,
        itemKey = item_key,
        eventType = event_type.toDomain()
    )
}

internal fun ItemEventType.toEntity(): net.irgaly.kkvs.data.sqlite.entity.ItemEventType {
    return when (this) {
        ItemEventType.Create -> net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Create
        ItemEventType.Update -> net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Update
        ItemEventType.Delete -> net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Delete
        ItemEventType.Expired -> net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Expired
    }
}

internal fun net.irgaly.kkvs.data.sqlite.entity.ItemEventType.toDomain(): ItemEventType {
    return when (this) {
        net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Create -> ItemEventType.Create
        net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Update -> ItemEventType.Update
        net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Delete -> ItemEventType.Delete
        net.irgaly.kkvs.data.sqlite.entity.ItemEventType.Expired -> ItemEventType.Expired
    }
}
