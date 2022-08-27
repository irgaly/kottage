package io.github.irgaly.kottage

import kotlinx.serialization.SerializationException
import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.model.Item
import kotlin.reflect.KType

class KkvsEntry<T : Any> internal constructor(
    private val item: Item,
    private val type: KType,
    private val encoder: Encoder
) {
    @Throws(
        ClassCastException::class,
        SerializationException::class
    )
    fun get(): T {
        return encoder.decode(item, type)
    }
}
