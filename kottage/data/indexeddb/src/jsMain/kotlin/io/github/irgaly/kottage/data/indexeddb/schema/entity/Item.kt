package io.github.irgaly.kottage.data.indexeddb.schema.entity

external interface Item {
    var key: String
    var type: String
    var string_value: String?

    // 64 bit 整数を保持するため Long を String で表現する
    // JavaScript は 53 bit 整数までしか表現できない
    var long_value: String?
    var double_value: Double?
    var bytes_value: ByteArray?
    var created_at: Double
    var last_read_at: Double
    var expire_at: Double?
}
