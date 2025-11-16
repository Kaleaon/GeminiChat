package com.example.aistudioapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aistudioapp.data.model.ProviderType

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "provider") val providerType: ProviderType = ProviderType.GEMINI,
    val isArchived: Boolean = false
)
