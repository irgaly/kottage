package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.data.indexeddb.extension.jso
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats
import io.github.irgaly.kottage.internal.model.ItemStats

internal fun io.github.irgaly.kottage.data.indexeddb.schema.entity.Item.toDomain(): Item {
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

internal fun Item.toEntity(): io.github.irgaly.kottage.data.indexeddb.schema.entity.Item {
    return jso {
        key = getEntityKey()
        type = type
        string_value = stringValue
        long_value = longValue
        double_value = doubleValue
        bytes_value = bytesValue
        created_at = createdAt
        last_read_at = lastReadAt
        expire_at = expireAt
    }
}

internal fun io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_list.toDomain(): ItemListEntry {
    return ItemListEntry(
        id = id,
        type = type,
        itemType = item_type,
        itemKey = item_key,
        previousId = previous_id,
        nextId = next_id,
        expireAt = expire_at,
        userInfo = user_info,
        userPreviousKey = user_previous_key,
        userCurrentKey = user_current_key,
        userNextKey = user_next_key
    )
}

internal fun ItemListEntry.toEntity(): io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_list {
    return jso {
        id = id
        type = type
        item_type = itemType
        item_key = itemKey
        previous_id = previousId
        next_id = nextId
        expire_at = expireAt
        user_info = userInfo
        user_previous_key = userPreviousKey
        user_current_key = userCurrentKey
        user_next_key = userNextKey
    }
}

internal fun io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_stats.toDomain(): ItemStats {
    return ItemStats(
        itemType = item_type,
        count = count,
        eventCount = event_count
    )
}

internal fun io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_list_stats.toDomain(): ItemListStats {
    return ItemListStats(
        listType = item_list_type,
        count = count,
        firstItemPositionId = first_item_list_id,
        lastItemPositionId = last_item_list_id
    )
}

internal fun io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_event.toDomain(): ItemEvent {
    return ItemEvent(
        id = id,
        createdAt = created_at,
        expireAt = expire_at,
        itemType = item_type,
        itemKey = item_key,
        itemListId = item_list_id,
        itemListType = item_list_type,
        eventType = io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.valueOf(event_type).toDomain()
    )
}

internal fun ItemEvent.toEntity(): io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_event {
    return jso {
        id = id
        created_at = createdAt
        expire_at = expireAt
        item_type = itemType
        item_key = itemKey
        item_list_id = itemListId
        item_list_type = itemListType
        event_type = eventType.toEntity().name
    }
}

internal fun ItemEventType.toEntity(): io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType {
    return when (this) {
        ItemEventType.Create -> io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.Create
        ItemEventType.Update -> io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.Update
        ItemEventType.Delete -> io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.Delete
    }
}

internal fun io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.toDomain(): ItemEventType {
    return when (this) {
        io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.Create -> ItemEventType.Create
        io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.Update -> ItemEventType.Update
        io.github.irgaly.kottage.data.indexeddb.schema.entity.ItemEventType.Delete -> ItemEventType.Delete
    }
}
