package com.example.aistudioapp.ui.creator.data

import android.content.Context
import com.example.aistudioapp.ui.creator.DesignerMode
import com.example.aistudioapp.ui.creator.model.DesignerSliderConfig
import com.example.aistudioapp.ui.creator.model.DesignerSliderSpec
import com.example.aistudioapp.ui.creator.model.SkeletonType
import kotlinx.serialization.json.Json

class DesignerSliderRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val assetPath = "characterstudio/designer_sliders.json"

    private val config: DesignerSliderConfig by lazy {
        context.assets.open(assetPath).use { stream ->
            val content = stream.bufferedReader().readText()
            json.decodeFromString(DesignerSliderConfig.serializer(), content)
        }
    }

    fun getSliders(mode: DesignerMode, skeletonType: SkeletonType): List<DesignerSliderSpec> {
        return when (mode) {
            DesignerMode.MAKEHUMAN -> config.makehuman
            DesignerMode.CHARACTER_STUDIO ->
                if (skeletonType == SkeletonType.SECOND_LIFE) {
                    config.characterStudioSecondLife
                } else {
                    config.characterStudio
                }
        }
    }
}
