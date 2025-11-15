package com.example.aistudioapp.di

import android.content.Context
import androidx.room.Room
import com.example.aistudioapp.data.chat.ConversationCondenser
import com.example.aistudioapp.data.drive.DriveBackupManager
import com.example.aistudioapp.data.files.AttachmentReader
import com.example.aistudioapp.data.local.ChatDatabase
import com.example.aistudioapp.data.prefs.AvatarPreferencesStore
import com.example.aistudioapp.data.prefs.SettingsDataStore
import com.example.aistudioapp.data.remote.AiProviderClient
import com.example.aistudioapp.data.repository.ChatRepository

object ServiceLocator {

    @Volatile
    private var chatDatabase: ChatDatabase? = null

    @Volatile
    private var chatRepository: ChatRepository? = null
    @Volatile
    private var avatarStore: AvatarPreferencesStore? = null

    fun initialize(context: Context) {
        provideChatRepository(context)
    }

    fun provideChatRepository(context: Context): ChatRepository {
        return chatRepository ?: synchronized(this) {
            chatRepository ?: createChatRepository(context.applicationContext).also {
                chatRepository = it
            }
        }
    }

    private fun createChatRepository(appContext: Context): ChatRepository {
        val database = chatDatabase ?: Room.databaseBuilder(
            appContext,
            ChatDatabase::class.java,
            "gemini_chat.db"
        )
            .fallbackToDestructiveMigration()
            .build()
            .also { chatDatabase = it }

        return ChatRepository(
            chatDao = database.chatDao(),
            settingsDataStore = SettingsDataStore(appContext),
            aiProviderClient = AiProviderClient(),
            attachmentReader = AttachmentReader(appContext),
            driveBackupManager = DriveBackupManager(appContext),
            condenser = ConversationCondenser()
        )
    }

    fun provideAvatarStore(context: Context): AvatarPreferencesStore {
        return avatarStore ?: synchronized(this) {
            avatarStore ?: AvatarPreferencesStore(context.applicationContext).also {
                avatarStore = it
            }
        }
    }
}
