package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val activeProvider: ProviderType = ProviderType.GEMINI,
    val providerConfigs: Map<ProviderType, ProviderConfig> = ProviderType.values().associateWith {
        ProviderConfig(providerType = it)
    },
    val systemPrompt: String = DEFAULT_PROMPT,
    val driveBackupState: DriveBackupState = DriveBackupState(),
    val allowImages: Boolean = true,
    val allowDocuments: Boolean = true,
    val enableCondensePrompt: Boolean = true,
    val autoCondenseThreshold: Int = 40,
    val marbleThemeEnabled: Boolean = true
) {
    companion object {
        private const val DEFAULT_PROMPT =
            "You are GeminiChat, a thoughtful assistant who compares ideas, summarizes attachments, and keeps replies concise."
    }
}
