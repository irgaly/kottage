package io.github.irgaly.kottage.sample.model

import kotlinx.serialization.Serializable

@Serializable
data class Animal(
    val id: String,
    val index: Long,
    val name: String
)
