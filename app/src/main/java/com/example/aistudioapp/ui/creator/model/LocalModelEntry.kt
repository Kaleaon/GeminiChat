package com.example.aistudioapp.ui.creator.model

data class LocalModelEntry(
    val displayName: String,
    val fileName: String,
    val extension: String,
    val webPath: String,
    val skeletonType: SkeletonType = SkeletonType.HUMAN
)

enum class SkeletonType {
    HUMAN,
    SECOND_LIFE
}
