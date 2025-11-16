package com.example.aistudioapp.data.drive

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.aistudioapp.data.model.ChatTranscript
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

class DriveBackupManager(
    private val context: Context,
    private val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
) {

    /**
     * Persists a transcript into the cached local storage for offline access.
     */
    suspend fun cacheTranscriptLocally(
        transcript: ChatTranscript
    ): Result<Uri> = runCatching {
        withContext(Dispatchers.IO) {
            val fileName = buildFileName(transcript)
            val file = context.getFileStreamPath(fileName)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                val payload = json.encodeToString(transcript)
                output.write(payload.toByteArray(StandardCharsets.UTF_8))
            }
            Uri.fromFile(file)
        }
    }

    /**
     * Uses Storage Access Framework so users can point to a Google Drive folder/tree and
     * persist chats directly in Drive.
     */
    suspend fun backupToDriveTree(
        contentResolver: ContentResolver,
        treeUri: Uri,
        transcript: ChatTranscript
    ): Result<Uri> = runCatching {
        withContext(Dispatchers.IO) {
            val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )

            val newFileUri = DocumentsContract.createDocument(
                contentResolver,
                parentDocumentUri,
                "application/json",
                buildFileName(transcript)
            ) ?: error("Unable to create document in selected Drive folder")

            contentResolver.openOutputStream(newFileUri)?.use { output ->
                val payload = json.encodeToString(transcript)
                output.write(payload.toByteArray(StandardCharsets.UTF_8))
            } ?: error("Unable to open document stream")

            newFileUri
        }
    }

    private fun buildFileName(transcript: ChatTranscript): String {
        val safeTitle = transcript.title.replace("[^A-Za-z0-9_-]".toRegex(), "-")
        return "GeminiChat-$safeTitle-${transcript.id}.json"
    }
}
