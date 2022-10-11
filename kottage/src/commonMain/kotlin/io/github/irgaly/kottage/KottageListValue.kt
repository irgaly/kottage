package io.github.irgaly.kottage

import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class KottageListValue<T : Any>(
    val key: String,
    val value: T,
    val type: KType,
    val metaData: KottageListMetaData? = null
)

inline fun <reified T : Any> kottageListValue(
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
): KottageListValue<T> {
    return KottageListValue(
        key = key,
        value = value,
        type = typeOf<T>(),
        metaData = metaData
    )
}
