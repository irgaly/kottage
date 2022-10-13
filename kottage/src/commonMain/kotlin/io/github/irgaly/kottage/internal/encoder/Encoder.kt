package io.github.irgaly.kottage.internal.encoder

import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.encoder.KottageEncoder
import io.github.irgaly.kottage.internal.model.Item
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.time.Duration

internal class Encoder(
    private val json: Json,
    private val userEncoder: KottageEncoder?
) {
    /**
     * @throws ClassCastException when type of value is different from type:KType
     * @throws SerializationException when serialization encoding is failed
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
        if (userEncoder != null) {
            bytesValue = when {
                (bytesValue != null) -> userEncoder.encode(bytesValue)
                (stringValue != null) -> userEncoder.encode(stringValue)
                (longValue != null) -> userEncoder.encode(longValue)
                (doubleValue != null) -> userEncoder.encode(doubleValue)
                else -> error("no value found")
            }
            stringValue = null
            longValue = null
            doubleValue = null
        }
        return block(stringValue, longValue, doubleValue, bytesValue)
    }

    /**
     * @throws ClassCastException when stored value's type is mismatch
     * @throws SerializationException when serialization decoding is failed
     */
    @Throws(
        ClassCastException::class,
        SerializationException::class
    )
    fun <T : Any> decode(item: Item, type: KType): T {
        val kClass = type.classifier
        fun decodeDouble(item: Item): Double? {
            return if (userEncoder != null) {
                val bytes = item.bytesValue
                    ?: throw ClassCastException("decode double from user encoded item = $item")
                userEncoder.decodeToDouble(bytes)
            } else item.doubleValue
        }

        fun decodeLong(item: Item): Long? {
            return if (userEncoder != null) {
                val bytes = item.bytesValue
                    ?: throw ClassCastException("decode long from user encoded item = $item")
                userEncoder.decodeToLong(bytes)
            } else item.longValue
        }

        fun decodeString(item: Item): String? {
            return if (userEncoder != null) {
                val bytes = item.bytesValue
                    ?: throw ClassCastException("decode string from user encoded item = $item")
                userEncoder.decodeToString(bytes)
            } else item.stringValue
        }

        fun decodeBytes(item: Item): ByteArray? {
            return if (userEncoder != null) {
                val bytes = item.bytesValue
                    ?: throw ClassCastException("decode byteArray from user encoded item = $item")
                userEncoder.decode(bytes)
            } else item.bytesValue
        }
        @Suppress("UNCHECKED_CAST")
        return when {
            (kClass == Double::class) -> {
                decodeDouble(item) ?: throw ClassCastException("double from item = $item")
            }

            (kClass == Float::class) -> {
                decodeDouble(item)?.toFloat() ?: throw ClassCastException("float from item = $item")
            }

            (kClass == Long::class) -> {
                decodeLong(item) ?: throw ClassCastException("long from item = $item")
            }

            (kClass == Int::class) -> {
                decodeLong(item)?.toInt() ?: throw ClassCastException("int from item = $item")
            }

            (kClass == Short::class) -> {
                decodeLong(item)?.toShort() ?: throw ClassCastException("short from item = $item")
            }

            (kClass == Byte::class) -> {
                decodeLong(item)?.toByte() ?: throw ClassCastException("byte from item = $item")
            }

            (kClass == Boolean::class) -> {
                val value =
                    decodeLong(item) ?: throw ClassCastException("boolean from item = $item")
                (value != 0L)
            }

            (kClass == ByteArray::class) -> {
                decodeBytes(item) ?: throw ClassCastException("byteArray from item = $item")
            }

            (kClass == String::class) -> {
                decodeString(item) ?: throw ClassCastException("string from item = $item")
            }

            else -> {
                val value =
                    decodeString(item) ?: throw ClassCastException("serializable from item = $item")
                json.decodeFromString(json.serializersModule.serializer(type), value)
            }
        } as T
    }
}

internal fun <T : Any> Encoder.encodeItem(
    storage: KottageStorage,
    itemKey: String,
    value: T,
    type: KType,
    now: Long,
    expireTime: Duration? = null
): Item {
    return encode(value, type) { stringValue: String?,
                                 longValue: Long?,
                                 doubleValue: Double?,
                                 bytesValue: ByteArray? ->
        Item(
            key = itemKey,
            type = storage.name,
            stringValue = stringValue,
            longValue = longValue,
            doubleValue = doubleValue,
            bytesValue = bytesValue,
            createdAt = now,
            lastReadAt = now,
            expireAt = (expireTime ?: storage.defaultExpireTime)?.let { duration ->
                now + duration.inWholeMilliseconds
            }
        )
    }
}
