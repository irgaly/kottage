package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.model.Item
import kotlinx.serialization.SerializationException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("unused")
class KottageEntry<T : Any> internal constructor(
    private val item: Item,
    private val type: KType,
    private val encoder: Encoder
) {
    val key: String get() = item.key
    val itemType: String get() = item.type
    val createdAt: Long get() = item.createdAt
    val lastReadAt: Long get() = item.lastReadAt
    val expireAt: Long? get() = item.expireAt

    @Throws(
        ClassCastException::class,
        SerializationException::class
    )
    fun get(): T {
        return encoder.decode(item, type)
    }

    fun <T : Any> withType(type: KType): KottageEntry<T> {
        return KottageEntry(
            item = item,
            type = type,
            encoder = encoder
        )
    }

    inline fun <reified T : Any> getWithType(): KottageEntry<T> = withType(type = typeOf<T>())
}
