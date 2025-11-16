package com.example.aistudioapp.ui.creator.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DesignerSliderConfig(
    val makehuman: List<DesignerSliderSpec> = emptyList(),
    @SerialName("characterStudio")
    val characterStudio: List<DesignerSliderSpec> = emptyList(),
    @SerialName("characterStudioSecondLife")
    val characterStudioSecondLife: List<DesignerSliderSpec> = emptyList()
)

@Serializable
data class DesignerSliderSpec(
    val id: String,
    val label: String,
    val min: Float,
    val max: Float,
    val defaultValue: Float = 0f,
    val step: Float = 0.05f,
    val targetScript: String
)
