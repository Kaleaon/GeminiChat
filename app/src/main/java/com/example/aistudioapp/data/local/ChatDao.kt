package com.example.aistudioapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.aistudioapp.data.local.entity.ChatMessageEntity
import com.example.aistudioapp.data.local.entity.ChatThreadEntity
import com.example.aistudioapp.data.local.model.ThreadWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_threads WHERE isArchived = 0 ORDER BY updated_at DESC")
    fun observeActiveThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    fun observeThread(threadId: Long): Flow<ChatThreadEntity?>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    suspend fun getThreadBlocking(threadId: Long): ChatThreadEntity?

    @Transaction
    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    fun observeThreadWithMessages(threadId: Long): Flow<ThreadWithMessages?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThread(entity: ChatThreadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>): List<Long>

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateThread(entity: ChatThreadEntity)

    @Delete
    suspend fun deleteThread(entity: ChatThreadEntity)

    @Query("DELETE FROM chat_messages WHERE thread_id = :threadId")
    suspend fun deleteMessagesForThread(threadId: Long)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE thread_id = :threadId")
    suspend fun countMessages(threadId: Long): Int

    @Query("SELECT * FROM chat_messages WHERE thread_id = :threadId ORDER BY created_at ASC")
    suspend fun getMessagesForThread(threadId: Long): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_threads ORDER BY updated_at DESC LIMIT 1")
    suspend fun getLatestThread(): ChatThreadEntity?
}
