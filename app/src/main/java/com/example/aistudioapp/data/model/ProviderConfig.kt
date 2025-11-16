package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProviderConfig(
    val providerType: ProviderType = ProviderType.GEMINI,
    val modelName: String = DEFAULT_MODEL_BY_PROVIDER[providerType] ?: "gemini-1.5-pro",
    val apiKey: String = "",
    val baseUrl: String? = null,
    val temperature: Float = 0.4f,
    val topP: Float = 0.95f,
    val topK: Int = 32,
    val maxOutputTokens: Int = 2048,
    val enableCensoring: Boolean = true,
    val safetyFilters: SafetyFilters = SafetyFilters(),
    val requestTimeoutSeconds: Long = 60L
) {
    companion object {
        val DEFAULT_MODEL_BY_PROVIDER = mapOf(
            ProviderType.GEMINI to "models/gemini-1.5-pro",
            ProviderType.CLAUDE to "claude-3-5-sonnet-20241022",
            ProviderType.META to "meta-llama/Llama-3.1-405B-Instruct",
            ProviderType.GROK to "grok-beta",
            ProviderType.OLLAMA to "llama3.1"
        )
    }
}
