package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SafetyFilters(
    val blockHate: Boolean = true,
    val blockViolence: Boolean = true,
    val blockSexual: Boolean = true,
    val blockSelfHarm: Boolean = true,
    val customBannedTerms: List<String> = emptyList()
)
