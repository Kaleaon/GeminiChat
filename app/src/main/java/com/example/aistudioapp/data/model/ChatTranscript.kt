package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatTranscript(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<ChatMessage>
)
