package io.github.irgaly.kottage

import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class KottageListEntry<T : Any>(
    val key: String,
    val value: T,
    val type: KType,
    val metaData: KottageListMetaData? = null
)

inline fun <reified T : Any> kottageListEntry(
    key: String,
    value: T,
    metaData: KottageListMetaData? = null
): KottageListEntry<T> {
    return KottageListEntry(
        key = key,
        value = value,
        type = typeOf<T>(),
        metaData = metaData
    )
}
