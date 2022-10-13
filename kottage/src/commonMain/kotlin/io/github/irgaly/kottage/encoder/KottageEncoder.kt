package io.github.irgaly.kottage.encoder

interface KottageEncoder {
    fun encode(value: ByteArray): ByteArray
    fun decode(encoded: ByteArray): ByteArray

    /**
     * default implementation: [String.encodeToByteArray] / UTF-8 encoding
     */
    fun encode(value: String): ByteArray {
        return encode(value.encodeToByteArray())
    }

    /**
     * default implementation: 8 bytes Little Endian
     */
    fun encode(value: Long): ByteArray {
        return encode(value.toBytes())
    }

    /**
     * default implementation: [Double.toRawBits] + 8 bytes Little Endian
     */
    fun encode(value: Double): ByteArray {
        return encode(value.toRawBits().toBytes())
    }

    /**
     * default implementation: [ByteArray.decodeToString] / UTF-8 decoding
     */
    fun decodeToString(encoded: ByteArray): String {
        return decode(encoded).decodeToString()
    }

    /**
     * default implementation: 8 bytes Little Endian
     */
    fun decodeToLong(encoded: ByteArray): Long {
        return decode(encoded).toLong()
    }

    /**
     * default implementation: [Double.Companion.fromBits] + 8 bytes Little Endian
     */
    fun decodeToDouble(encoded: ByteArray): Double {
        return Double.fromBits(decode(encoded).toLong())
    }

    /**
     * 8 bytes Little Endian
     */
    private fun Long.toBytes(): ByteArray {
        val result = ByteArray(8)
        var value = this
        (0..7).forEach {
            result[it] = (value and 0xFF).toByte()
            value = value shr 8
        }
        return result
    }

    /**
     * 8 bytes Little Endian
     */
    private fun ByteArray.toLong(): Long {
        var result = 0L
        (0..7.coerceAtMost(lastIndex)).forEach {
            result = result or (this[it].toUByte().toLong() shl (8 * it))
        }
        return result
    }
}
