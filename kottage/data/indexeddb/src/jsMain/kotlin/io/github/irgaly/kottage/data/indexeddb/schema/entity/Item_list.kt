package io.github.irgaly.kottage.data.indexeddb.schema.entity

external interface Item_list {
  var id: String
  var type: String
  var item_type: String
  var item_key: String?
  var previous_id: String?
  var next_id: String?
  var expire_at: Double?
  var user_info: String?
  var user_previous_key: String?
  var user_current_key: String?
  var user_next_key: String?
}
