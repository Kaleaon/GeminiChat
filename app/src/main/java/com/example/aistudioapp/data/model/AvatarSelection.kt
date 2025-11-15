package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AvatarSelection(
    val type: AvatarType,
    val label: String,
    val payload: String,
    val updatedAt: Long = System.currentTimeMillis()
)
