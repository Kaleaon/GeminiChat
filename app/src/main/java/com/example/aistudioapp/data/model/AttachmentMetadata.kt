package com.example.aistudioapp.data.model

import android.net.Uri
import kotlinx.serialization.Serializable

/**
 * Represents a file attachment that can be associated with a chat message.
 */
@Serializable
data class AttachmentMetadata(
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val type: AttachmentType,
    val sizeInBytes: Long? = null
) {
    val parsedUri: Uri
        get() = Uri.parse(uri)
}
