package com.example.aistudioapp.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.aistudioapp.data.local.entity.ChatMessageEntity
import com.example.aistudioapp.data.local.entity.ChatThreadEntity

data class ThreadWithMessages(
    @Embedded val thread: ChatThreadEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "thread_id"
    )
    val messages: List<ChatMessageEntity>
)
