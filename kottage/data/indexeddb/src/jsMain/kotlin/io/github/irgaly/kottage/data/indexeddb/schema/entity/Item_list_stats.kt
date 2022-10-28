package io.github.irgaly.kottage.data.indexeddb.schema.entity

external interface Item_list_stats {
  var item_list_type: String
  var count: Long
  var first_item_list_id: String
  var last_item_list_id: String
}
