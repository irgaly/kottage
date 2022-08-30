package io.github.irgaly.kottage.internal.encoder

import io.github.irgaly.kottage.internal.model.Item
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

internal class Encoder(private val json: Json) {
    /**
     * @throws ClassCastException when type of value is different from type:KType
     * @throws SerializationException when serialization endcoding is failed
     */
    @Throws(ClassCastException::class, SerializationException::class)
    fun <T : Any> encode(
        value: T,
        type: KType,
        block: (
            stringValue: String?,
            longValue: Long?,
            doubleValue: Double?,
            bytesValue: ByteArray?
        ) -> Item
    ): Item {
        val kClass = type.classifier
        var stringValue: String? = null
        var longValue: Long? = null
        var doubleValue: Double? = null
        var bytesValue: ByteArray? = null
        when {
            (kClass == Double::class) -> {
                doubleValue = (value as Double)
            }
            (kClass == Float::class) -> {
                doubleValue = (value as Float).toDouble()
            }
            (kClass == Long::class) -> {
                longValue = (value as Long)
            }
            (kClass == Int::class) -> {
                longValue = (value as Int).toLong()
            }
            (kClass == Short::class) -> {
                longValue = (value as Short).toLong()
            }
            (kClass == Byte::class) -> {
                longValue = (value as Byte).toLong()
            }
            (kClass == Boolean::class) -> {
                longValue = if (value as Boolean) 1L else 0L
            }
            (kClass == ByteArray::class) -> {
                bytesValue = (value as ByteArray)
            }
            (kClass == String::class) -> {
                stringValue = (value as String)
            }
            else -> {
                stringValue = json.encodeToString(json.serializersModule.serializer(type), value)
            }
        }
        return block(stringValue, longValue, doubleValue, bytesValue)
    }

    /**
     * @throws ClassCastException when stored value's type is mismatch
     * @throws SerializationException when serialization decoding is failed
     */
    @Throws(ClassCastException::class, SerializationException::class)
    fun <T : Any> decode(item: Item, type: KType): T {
        val kClass = type.classifier
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        return when {
            (kClass == Double::class) -> {
                item.doubleValue ?: throw ClassCastException("double from item = $item")
            }
            (kClass == Float::class) -> {
                item.doubleValue?.toFloat() ?: throw ClassCastException("float from item = $item")
            }
            (kClass == Long::class) -> {
                item.longValue ?: throw ClassCastException("long from item = $item")
            }
            (kClass == Int::class) -> {
                item.longValue?.toInt() ?: throw ClassCastException("int from item = $item")
            }
            (kClass == Short::class) -> {
                item.longValue?.toShort() ?: throw ClassCastException("short from item = $item")
            }
            (kClass == Byte::class) -> {
                item.longValue?.toByte() ?: throw ClassCastException("byte from item = $item")
            }
            (kClass == Boolean::class) -> {
                val value = item.longValue ?: throw ClassCastException("boolean from item = $item")
                (value != 0L)
            }
            (kClass == ByteArray::class) -> {
                item.bytesValue ?: throw ClassCastException("byteArray from item = $item")
            }
            (kClass == String::class) -> {
                item.stringValue ?: throw ClassCastException("string from item = $item")
            }
            else -> {
                val value = item.stringValue ?: throw ClassCastException("serializable from item = $item")
                json.decodeFromString(json.serializersModule.serializer(type), value)
            }
        } as T
    }
}
