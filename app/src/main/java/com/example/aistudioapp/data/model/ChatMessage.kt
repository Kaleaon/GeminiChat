package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ChatMessage(
    val id: Long? = null,
    val role: ChatRole,
    val content: String,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val attachments: List<AttachmentMetadata> = emptyList(),
    val providerType: ProviderType? = null,
    val metadata: Map<String, String> = emptyMap()
)
