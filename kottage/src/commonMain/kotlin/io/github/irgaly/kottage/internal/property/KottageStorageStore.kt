package io.github.irgaly.kottage.internal.property

import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.property.KottageStore
import kotlin.reflect.KType
import kotlin.time.Duration

internal class KottageStorageStore<T> (
    private val storage: KottageStorage,
    private val key: String,
    private val type: KType,
    private val expireTime: Duration?,
    private val defaultValue: () -> T
): KottageStore<T> {
    override suspend fun read(): T {
        return storage.getOrNull(key, type) ?: defaultValue()
    }

    override suspend fun write(value: T) {
        if (value == null) {
            storage.remove(key)
        } else {
            storage.put(key, value, type, expireTime = expireTime)
        }
    }

    override suspend fun exists(): Boolean {
        return (storage.exists(key) || (defaultValue() != null))
    }

    override suspend fun clear() {
        storage.remove(key)
    }
}
