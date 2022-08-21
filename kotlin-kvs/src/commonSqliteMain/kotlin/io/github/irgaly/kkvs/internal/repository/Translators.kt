package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.model.Item
import io.github.irgaly.kkvs.internal.model.ItemEvent
import io.github.irgaly.kkvs.internal.model.ItemEventType

internal fun io.github.irgaly.kkvs.data.sqlite.Item.toDomain(): Item {
    return Item(
        key = Item.fromEntityKey(key, type),
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

internal fun Item.toEntity(): io.github.irgaly.kkvs.data.sqlite.Item {
    return io.github.irgaly.kkvs.data.sqlite.Item(
        key = getEntityKey(),
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

internal fun io.github.irgaly.kkvs.data.sqlite.Item_event.toDomain(): ItemEvent {
    return ItemEvent(
        createdAt = created_at,
        itemType = item_type,
        itemKey = Item.fromEntityKey(item_key, item_type),
        eventType = event_type.toDomain()
    )
}

internal fun ItemEvent.toEntity(): io.github.irgaly.kkvs.data.sqlite.Item_event {
    return io.github.irgaly.kkvs.data.sqlite.Item_event(
        created_at = createdAt,
        item_type = itemType,
        item_key = Item.toEntityKey(itemKey, itemType),
        event_type = eventType.toEntity()
    )
}

internal fun ItemEventType.toEntity(): io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType {
    return when (this) {
        ItemEventType.Create -> io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Create
        ItemEventType.Update -> io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Update
        ItemEventType.Delete -> io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Delete
        ItemEventType.Expired -> io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Expired
    }
}

internal fun io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.toDomain(): ItemEventType {
    return when (this) {
        io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Create -> ItemEventType.Create
        io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Update -> ItemEventType.Update
        io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Delete -> ItemEventType.Delete
        io.github.irgaly.kkvs.data.sqlite.entity.ItemEventType.Expired -> ItemEventType.Expired
    }
}
