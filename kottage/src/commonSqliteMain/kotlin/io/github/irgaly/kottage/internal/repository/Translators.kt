package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal fun io.github.irgaly.kottage.data.sqlite.Item.toDomain(): Item {
    return Item(
        key = Item.keyFromEntityKey(key, type),
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

internal fun Item.toEntity(): io.github.irgaly.kottage.data.sqlite.Item {
    return io.github.irgaly.kottage.data.sqlite.Item(
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

internal fun io.github.irgaly.kottage.data.sqlite.Item_list.toDomain(): ItemListEntry {
    return ItemListEntry(
        id = id,
        type = type,
        itemType = item_type,
        itemKey = item_key,
        previousId = previous_id,
        nextId = next_id,
        expireAt = expire_at,
        userPreviousKey = user_previous_key,
        userCurrentKey = user_current_key,
        userNextKey = user_next_key
    )
}

internal fun ItemListEntry.toEntity(): io.github.irgaly.kottage.data.sqlite.Item_list {
    return io.github.irgaly.kottage.data.sqlite.Item_list(
        id = id,
        type = type,
        item_type = itemType,
        item_key = itemKey,
        previous_id = previousId,
        next_id = nextId,
        expire_at = expireAt,
        user_previous_key = userPreviousKey,
        user_current_key = userCurrentKey,
        user_next_key = userNextKey
    )
}

internal fun io.github.irgaly.kottage.data.sqlite.Item_list_stats.toDomain(): ItemListStats {
    return ItemListStats(
        listType = item_list_type,
        count = count,
        firstItemPositionId = first_item_list_id,
        lastItemPositionId = last_item_list_id
    )
}

internal fun io.github.irgaly.kottage.data.sqlite.Item_event.toDomain(): ItemEvent {
    return ItemEvent(
        id = id,
        createdAt = created_at,
        expireAt = expire_at,
        itemType = item_type,
        itemKey = Item.keyFromEntityKey(item_key, item_type),
        itemListId = item_list_id,
        itemListType = item_list_type,
        eventType = event_type.toDomain()
    )
}

internal fun ItemEvent.toEntity(): io.github.irgaly.kottage.data.sqlite.Item_event {
    return io.github.irgaly.kottage.data.sqlite.Item_event(
        id = id,
        created_at = createdAt,
        expire_at = expireAt,
        item_type = itemType,
        item_key = Item.toEntityKey(itemKey, itemType),
        item_list_id = null,
        item_list_type = null,
        event_type = eventType.toEntity()
    )
}

internal fun ItemEventType.toEntity(): io.github.irgaly.kottage.data.sqlite.entity.ItemEventType {
    return when (this) {
        ItemEventType.Create -> io.github.irgaly.kottage.data.sqlite.entity.ItemEventType.Create
        ItemEventType.Update -> io.github.irgaly.kottage.data.sqlite.entity.ItemEventType.Update
        ItemEventType.Delete -> io.github.irgaly.kottage.data.sqlite.entity.ItemEventType.Delete
    }
}

internal fun io.github.irgaly.kottage.data.sqlite.entity.ItemEventType.toDomain(): ItemEventType {
    return when (this) {
        io.github.irgaly.kottage.data.sqlite.entity.ItemEventType.Create -> ItemEventType.Create
        io.github.irgaly.kottage.data.sqlite.entity.ItemEventType.Update -> ItemEventType.Update
        io.github.irgaly.kottage.data.sqlite.entity.ItemEventType.Delete -> ItemEventType.Delete
    }
}
