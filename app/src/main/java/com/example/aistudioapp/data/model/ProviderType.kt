package com.example.aistudioapp.data.model

import androidx.annotation.StringRes
import com.example.aistudioapp.R

enum class ProviderType(@StringRes val displayNameRes: Int) {
    GEMINI(R.string.provider_gemini),
    CLAUDE(R.string.provider_claude),
    META(R.string.provider_meta),
    GROK(R.string.provider_grok),
    OLLAMA(R.string.provider_ollama)
}
