package io.github.irgaly.kottage.data.indexeddb.schema.entity

external interface Item_event {
  var id: String
  var created_at: Long
  var expire_at: Long?
  var item_type: String
  var item_key: String
  var item_list_id: String?
  var item_list_type: String?
  var event_type: String // ItemEventType
}
