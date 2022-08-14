package net.irgaly.kkvs

data class KkvsEntry <T: Any> (
    val value: T?
) {
    fun get(): T {
        return checkNotNull(value)
    }
}
