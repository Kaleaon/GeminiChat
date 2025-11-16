package com.example.aistudioapp.data.local

import androidx.room.TypeConverter
import com.example.aistudioapp.data.model.AttachmentMetadata
import com.example.aistudioapp.data.model.ChatMessage
import com.example.aistudioapp.data.model.ChatRole
import com.example.aistudioapp.data.model.ProviderType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class AppTypeConverters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromProviderType(value: ProviderType?): String? = value?.name

    @TypeConverter
    fun toProviderType(value: String?): ProviderType? = value?.let { ProviderType.valueOf(it) }

    @TypeConverter
    fun fromChatRole(value: ChatRole): String = value.name

    @TypeConverter
    fun toChatRole(value: String): ChatRole = ChatRole.valueOf(value)

    @TypeConverter
    fun attachmentsToJson(value: List<AttachmentMetadata>): String =
        json.encodeToString(ListSerializer(AttachmentMetadata.serializer()), value)

    @TypeConverter
    fun attachmentsFromJson(value: String): List<AttachmentMetadata> =
        runCatching {
            json.decodeFromString(ListSerializer(AttachmentMetadata.serializer()), value)
        }.getOrDefault(emptyList())

    @TypeConverter
    fun metadataToJson(value: Map<String, String>): String =
        json.encodeToString(MapSerializer(String.serializer(), String.serializer()), value)

    @TypeConverter
    fun metadataFromJson(value: String): Map<String, String> =
        runCatching {
            json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), value)
        }.getOrDefault(emptyMap())

    @TypeConverter
    fun messagesToJson(value: List<ChatMessage>): String =
        json.encodeToString(ListSerializer(ChatMessage.serializer()), value)

    @TypeConverter
    fun messagesFromJson(value: String): List<ChatMessage> =
        runCatching {
            json.decodeFromString(ListSerializer(ChatMessage.serializer()), value)
        }.getOrDefault(emptyList())
}
