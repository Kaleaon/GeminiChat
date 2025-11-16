package com.example.aistudioapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DriveBackupState(
    val lastBackupAt: Long = 0L,
    val lastBackupFileUri: String? = null,
    val isAutoBackupEnabled: Boolean = true,
    val folderTreeUri: String? = null
)
