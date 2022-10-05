package io.github.irgaly.kottage

data class KottageListEntry<T: Any>(
    val key: String,
    val value: T,
    val metaData: KottageListMetaData? = null
)
