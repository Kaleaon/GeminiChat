package com.example.aistudioapp.data.remote

import com.example.aistudioapp.data.model.ChatMessage
import com.example.aistudioapp.data.model.ChatRole
import com.example.aistudioapp.data.model.ProviderConfig
import com.example.aistudioapp.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiProviderClient(
    okHttpClient: OkHttpClient? = null
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = okHttpClient ?: OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun sendChat(
        providerConfig: ProviderConfig,
        history: List<ChatMessage>
    ): ChatMessage = withContext(Dispatchers.IO) {
        require(providerConfig.apiKey.isNotBlank() || providerConfig.providerType == ProviderType.OLLAMA) {
            "API key required for ${providerConfig.providerType.name}"
        }

        val content = when (providerConfig.providerType) {
            ProviderType.GEMINI -> callGemini(providerConfig, history)
            ProviderType.CLAUDE -> callClaude(providerConfig, history)
            ProviderType.META -> callMeta(providerConfig, history)
            ProviderType.GROK -> callGrok(providerConfig, history)
            ProviderType.OLLAMA -> callOllama(providerConfig, history)
        }

        ChatMessage(
            role = ChatRole.ASSISTANT,
            content = content,
            providerType = providerConfig.providerType
        )
    }

    private fun buildMessagesArray(history: List<ChatMessage>): JSONArray {
        val messages = JSONArray()
        history.forEach { message ->
            messages.put(
                JSONObject().apply {
                    put("role", when (message.role) {
                        ChatRole.USER -> "user"
                        ChatRole.ASSISTANT -> "assistant"
                        ChatRole.SYSTEM -> "system"
                    })
                    put("content", message.content)
                }
            )
        }
        return messages
    }

    private fun JSONArray.wrapForGemini(): JSONArray {
        val transformed = JSONArray()
        for (i in 0 until length()) {
            val item = getJSONObject(i)
            val parts = JSONArray().apply {
                put(JSONObject().apply { put("text", item.getString("content")) })
            }
            transformed.put(
                JSONObject().apply {
                    put("role", item.getString("role"))
                    put("parts", parts)
                }
            )
        }
        return transformed
    }

    private fun JSONObject.toRequestBody(): RequestBody = toString().toRequestBody(jsonMediaType)

    @Throws(IOException::class)
    private fun callGemini(
        providerConfig: ProviderConfig,
        history: List<ChatMessage>
    ): String {
        val baseUrl = providerConfig.baseUrl?.trimEnd('/')
            ?: "https://generativelanguage.googleapis.com"
        val requestUrl = "$baseUrl/v1beta/${providerConfig.modelName}:generateContent?key=${providerConfig.apiKey}"

        val payload = JSONObject().apply {
            put("contents", buildMessagesArray(history).wrapForGemini())
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", providerConfig.temperature)
                    put("topP", providerConfig.topP)
                    put("topK", providerConfig.topK)
                    put("maxOutputTokens", providerConfig.maxOutputTokens)
                }
            )
            if (providerConfig.safetyFilters.customBannedTerms.isNotEmpty() ||
                providerConfig.safetyFilters.blockHate ||
                providerConfig.safetyFilters.blockSelfHarm ||
                providerConfig.safetyFilters.blockSexual ||
                providerConfig.safetyFilters.blockViolence
            ) {
                put("safetySettings", buildGeminiSafety(providerConfig))
            }
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(payload.toRequestBody())
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Gemini error: ${response.code} ${response.message}")
            }

            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val candidates = json.optJSONArray("candidates") ?: return ""
            val first = candidates.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
            return first ?: ""
        }
    }

    private fun buildGeminiSafety(providerConfig: ProviderConfig): JSONArray {
        val safety = JSONArray()
        if (providerConfig.safetyFilters.blockHate) {
            safety.put(
                JSONObject().apply {
                    put("category", "HARM_CATEGORY_HATE_SPEECH")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                }
            )
        }
        if (providerConfig.safetyFilters.blockSelfHarm) {
            safety.put(
                JSONObject().apply {
                    put("category", "HARM_CATEGORY_SELF_HARM")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                }
            )
        }
        if (providerConfig.safetyFilters.blockSexual) {
            safety.put(
                JSONObject().apply {
                    put("category", "HARM_CATEGORY_SEXUAL")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                }
            )
        }
        if (providerConfig.safetyFilters.blockViolence) {
            safety.put(
                JSONObject().apply {
                    put("category", "HARM_CATEGORY_VIOLENCE")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                }
            )
        }

        providerConfig.safetyFilters.customBannedTerms.takeIf { it.isNotEmpty() }?.let { terms ->
            safety.put(
                JSONObject().apply {
                    put("category", "HARM_CATEGORY_DEROGATORY")
                    put("threshold", "BLOCK_LOW_AND_ABOVE")
                    put("bannedPhrases", JSONArray(terms))
                }
            )
        }
        return safety
    }

    @Throws(IOException::class)
    private fun callClaude(
        providerConfig: ProviderConfig,
        history: List<ChatMessage>
    ): String {
        val baseUrl = providerConfig.baseUrl?.trimEnd('/')
            ?: "https://api.anthropic.com"
        val requestUrl = "$baseUrl/v1/messages"

        val payload = JSONObject().apply {
            put("model", providerConfig.modelName)
            put("max_tokens", providerConfig.maxOutputTokens)
            put("temperature", providerConfig.temperature)
            put(
                "messages",
                JSONArray().apply {
                    history.forEach { message ->
                        put(
                            JSONObject().apply {
                                put("role", when (message.role) {
                                    ChatRole.USER -> "user"
                                    ChatRole.ASSISTANT -> "assistant"
                                    ChatRole.SYSTEM -> "assistant"
                                })
                                put(
                                    "content",
                                    JSONArray().apply {
                                        put(
                                            JSONObject().apply {
                                                put("type", "text")
                                                put("text", message.content)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
            )
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(payload.toRequestBody())
            .addHeader("x-api-key", providerConfig.apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Claude error: ${response.code} ${response.message}")
            }

            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val content = json.optJSONArray("content")
                ?.optJSONObject(0)
                ?.optString("text")
            return content ?: ""
        }
    }

    @Throws(IOException::class)
    private fun callMeta(
        providerConfig: ProviderConfig,
        history: List<ChatMessage>
    ): String {
        val baseUrl = providerConfig.baseUrl?.trimEnd('/')
            ?: "https://api.meta.ai"
        val requestUrl = "$baseUrl/v1/chat/completions"

        val payload = JSONObject().apply {
            put("model", providerConfig.modelName)
            put("messages", buildMessagesArray(history))
            put("temperature", providerConfig.temperature)
            put("top_p", providerConfig.topP)
            put("max_tokens", providerConfig.maxOutputTokens)
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(payload.toRequestBody())
            .addHeader("Authorization", "Bearer ${providerConfig.apiKey}")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Meta AI error: ${response.code} ${response.message}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val choices = json.optJSONArray("choices")
            val first = choices?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
            return first ?: ""
        }
    }

    @Throws(IOException::class)
    private fun callGrok(
        providerConfig: ProviderConfig,
        history: List<ChatMessage>
    ): String {
        val baseUrl = providerConfig.baseUrl?.trimEnd('/')
            ?: "https://api.x.ai"
        val requestUrl = "$baseUrl/v1/chat/completions"

        val payload = JSONObject().apply {
            put("model", providerConfig.modelName)
            put("messages", buildMessagesArray(history))
            put("temperature", providerConfig.temperature)
            put("max_tokens", providerConfig.maxOutputTokens)
            put("stream", false)
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(payload.toRequestBody())
            .addHeader("Authorization", "Bearer ${providerConfig.apiKey}")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Grok error: ${response.code} ${response.message}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val choices = json.optJSONArray("choices")
            val first = choices?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
            return first ?: ""
        }
    }

    @Throws(IOException::class)
    private fun callOllama(
        providerConfig: ProviderConfig,
        history: List<ChatMessage>
    ): String {
        val baseUrl = providerConfig.baseUrl?.trimEnd('/')
            ?: "http://localhost:11434"
        val requestUrl = "$baseUrl/api/chat"

        val payload = JSONObject().apply {
            put("model", providerConfig.modelName)
            put("messages", buildMessagesArray(history))
            put("stream", false)
            put("options", JSONObject().apply {
                put("temperature", providerConfig.temperature)
                put("top_p", providerConfig.topP)
            })
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(payload.toRequestBody())
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Ollama error: ${response.code} ${response.message}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val message = json.optJSONObject("message")
            return message?.optString("content").orEmpty()
        }
    }
}
