package com.example.aistudioapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.aistudioapp.data.model.AttachmentMetadata
import com.example.aistudioapp.data.model.ChatRole
import com.example.aistudioapp.data.model.ProviderType

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["thread_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("thread_id"),
        Index("created_at")
    ]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "thread_id") val threadId: Long,
    val role: ChatRole,
    val content: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "provider") val providerType: ProviderType? = null,
    @ColumnInfo(name = "attachments") val attachments: List<AttachmentMetadata> = emptyList(),
    @ColumnInfo(name = "metadata") val metadata: Map<String, String> = emptyMap()
)
