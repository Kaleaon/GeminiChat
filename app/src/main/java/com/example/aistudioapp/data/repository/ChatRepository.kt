package com.example.aistudioapp.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.aistudioapp.data.chat.ConversationCondenser
import com.example.aistudioapp.data.drive.DriveBackupManager
import com.example.aistudioapp.data.files.AttachmentReader
import com.example.aistudioapp.data.local.ChatDao
import com.example.aistudioapp.data.local.entity.ChatMessageEntity
import com.example.aistudioapp.data.local.entity.ChatThreadEntity
import com.example.aistudioapp.data.local.model.ThreadWithMessages
import com.example.aistudioapp.data.model.AppSettings
import com.example.aistudioapp.data.model.AttachmentMetadata
import com.example.aistudioapp.data.model.ChatMessage
import com.example.aistudioapp.data.model.ChatRole
import com.example.aistudioapp.data.model.ChatThread
import com.example.aistudioapp.data.model.ChatTranscript
import com.example.aistudioapp.data.model.ProviderConfig
import com.example.aistudioapp.data.model.ProviderType
import com.example.aistudioapp.data.prefs.SettingsDataStore
import com.example.aistudioapp.data.remote.AiProviderClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant

class ChatRepository(
    private val chatDao: ChatDao,
    private val settingsDataStore: SettingsDataStore,
    private val aiProviderClient: AiProviderClient,
    private val attachmentReader: AttachmentReader,
    private val driveBackupManager: DriveBackupManager,
    private val condenser: ConversationCondenser = ConversationCondenser()
) {

    val settingsFlow: Flow<AppSettings> = settingsDataStore.appSettings

    val threads: Flow<List<ChatThread>> = chatDao.observeActiveThreads().map { list ->
        list.map { it.toModel() }
    }

    fun observeMessages(threadId: Long): Flow<List<ChatMessage>> =
        chatDao.observeThreadWithMessages(threadId).map { threadWithMessages ->
            threadWithMessages?.messages?.map { it.toModel() } ?: emptyList()
        }

    fun observeThread(threadId: Long): Flow<ChatThread?> =
        chatDao.observeThread(threadId).map { it?.toModel() }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        settingsDataStore.update(transform)
    }

    suspend fun sendMessage(
        existingThreadId: Long?,
        prompt: String,
        attachments: List<AttachmentMetadata>
    ): SendMessageResult {
        val appSettings = settingsDataStore.appSettings.first()
        val providerConfig = appSettings.providerConfigs[appSettings.activeProvider]
            ?: ProviderConfig(providerType = appSettings.activeProvider)

        val threadId = existingThreadId ?: createThreadFromPrompt(
            prompt = prompt,
            providerType = providerConfig.providerType
        )

        val attachmentContext = attachmentReader.summarizeAttachments(attachments)
        val composedPrompt = buildString {
            append(prompt.trim())
            if (attachmentContext.isNotEmpty()) {
                appendLine()
                appendLine()
                appendLine("Attachment context:")
                attachmentContext.forEach { (name, content) ->
                    appendLine("[$name]")
                    appendLine(content)
                    appendLine()
                }
            }
        }

        val userMessage = ChatMessage(
            role = ChatRole.USER,
            content = composedPrompt.trim(),
            attachments = attachments,
            createdAt = Instant.now().toEpochMilli(),
            providerType = providerConfig.providerType
        )

        val userMessageId = chatDao.insertMessage(userMessage.toEntity(threadId))

        val history = chatDao.getMessagesForThread(threadId).map { it.toModel() }

        val assistantMessage = aiProviderClient.sendChat(
            providerConfig = providerConfig,
            history = history
        )

        val assistantMessageId = chatDao.insertMessage(
            assistantMessage.copy(createdAt = Instant.now().toEpochMilli())
                .toEntity(threadId)
        )

        chatDao.getThreadBlocking(threadId)?.let { thread ->
            chatDao.updateThread(
                thread.copy(
                    updatedAt = Instant.now().toEpochMilli()
                )
            )
        }

        return SendMessageResult(
            threadId = threadId,
            assistantMessage = assistantMessage,
            userMessageId = userMessageId,
            assistantMessageId = assistantMessageId
        )
    }

    suspend fun condenseThread(threadId: Long) {
        val messages = chatDao.getMessagesForThread(threadId).map { it.toModel() }
        if (messages.isEmpty()) return
        val condensed = condenser.condense(messages)

        chatDao.deleteMessagesForThread(threadId)
        chatDao.insertMessages(condensed.map { it.toEntity(threadId) })
    }

    suspend fun exportTranscriptToDrive(
        threadId: Long,
        contentResolver: ContentResolver,
        treeUri: Uri
    ): Result<Uri> {
        val transcript = getTranscript(threadId) ?: return Result.failure(
            IllegalStateException("Thread not found")
        )
        return driveBackupManager.backupToDriveTree(contentResolver, treeUri, transcript)
    }

    suspend fun cacheTranscript(threadId: Long): Result<Uri> {
        val transcript = getTranscript(threadId) ?: return Result.failure(
            IllegalStateException("Thread not found")
        )
        return driveBackupManager.cacheTranscriptLocally(transcript)
    }

    suspend fun cacheLatestTranscript(): Result<Uri> {
        val latest = chatDao.getLatestThread() ?: return Result.failure(
            IllegalStateException("No conversations to cache yet")
        )
        return cacheTranscript(latest.id)
    }

    suspend fun backupLatestTranscript(
        contentResolver: ContentResolver,
        treeUri: Uri
    ): Result<Uri> {
        val latest = chatDao.getLatestThread() ?: return Result.failure(
            IllegalStateException("No conversations to back up")
        )
        return exportTranscriptToDrive(latest.id, contentResolver, treeUri)
    }

    private suspend fun getTranscript(threadId: Long): ChatTranscript? {
        val threadWithMessages = chatDao.observeThreadWithMessages(threadId).first()
            ?: return null
        return threadWithMessages.toTranscript()
    }

    private suspend fun createThreadFromPrompt(
        prompt: String,
        providerType: ProviderType
    ): Long {
        val now = Instant.now().toEpochMilli()
        val entity = ChatThreadEntity(
            title = prompt.take(34).ifBlank { "New chat" },
            createdAt = now,
            updatedAt = now,
            providerType = providerType,
            isArchived = false
        )
        return chatDao.upsertThread(entity)
    }

    data class SendMessageResult(
        val threadId: Long,
        val assistantMessage: ChatMessage,
        val userMessageId: Long,
        val assistantMessageId: Long
    )

    private fun ChatThreadEntity.toModel(): ChatThread = ChatThread(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        providerType = providerType
    )

    private fun ChatMessageEntity.toModel(): ChatMessage = ChatMessage(
        id = id,
        role = role,
        content = content,
        createdAt = createdAt,
        providerType = providerType,
        attachments = attachments,
        metadata = metadata
    )

    private fun ChatMessage.toEntity(threadId: Long): ChatMessageEntity = ChatMessageEntity(
        threadId = threadId,
        role = role,
        content = content,
        createdAt = createdAt,
        providerType = providerType,
        attachments = attachments,
        metadata = metadata
    )

    private fun ThreadWithMessages.toTranscript(): ChatTranscript = ChatTranscript(
        id = thread.id,
        title = thread.title,
        createdAt = thread.createdAt,
        updatedAt = thread.updatedAt,
        messages = messages.sortedBy { it.createdAt }.map { it.toModel() }
    )
}
