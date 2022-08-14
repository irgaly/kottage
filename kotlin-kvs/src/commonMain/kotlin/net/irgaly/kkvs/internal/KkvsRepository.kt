package net.irgaly.kkvs.internal

interface KkvsRepository {
    suspend fun exists(key: String): Boolean
    suspend fun delete(key: String)
}
