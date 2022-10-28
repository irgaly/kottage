package io.github.irgaly.kottage.data.indexeddb.schema.entity

external interface Item{
    var key: String
    var type: String
    var string_value: String?
    var long_value: Long?
    var double_value: Double?
    var bytes_value: ByteArray?
    var created_at: Long
    var last_read_at: Long
    var expire_at: Long?
}
