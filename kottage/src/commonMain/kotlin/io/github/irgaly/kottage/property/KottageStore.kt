package io.github.irgaly.kottage.property

interface KottageStore<T> {
    suspend fun read(): T
    suspend fun write(value: T)
    suspend fun exists(): Boolean
    suspend fun clear()
}
