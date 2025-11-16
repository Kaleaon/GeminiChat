package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM
}
