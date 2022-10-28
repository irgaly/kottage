package io.github.irgaly.kottage.data.indexeddb.schema.entity

external interface Stats {
  var key: String
  var last_evict_at: Long
}
