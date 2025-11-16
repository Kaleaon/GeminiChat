package com.example.aistudioapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aistudioapp.data.local.entity.ChatMessageEntity
import com.example.aistudioapp.data.local.entity.ChatThreadEntity

@Database(
    entities = [
        ChatThreadEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
