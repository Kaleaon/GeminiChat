package com.example.aistudioapp.ui.creator.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.example.aistudioapp.ui.creator.model.LocalModelEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ModelLibraryRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val modelsDir: File = File(context.filesDir, "models").apply { mkdirs() }
    private val _models = MutableStateFlow<List<LocalModelEntry>>(emptyList())
    val models: StateFlow<List<LocalModelEntry>> = _models

    private val supportedExtensions = setOf(
        "obj", "stl", "ply", "dae", "blend", "fbx", "glb", "gltf"
    )

    init {
        scope.launch { refresh() }
    }

    suspend fun addModels(resolver: ContentResolver, uris: List<Uri>): Int {
        var imported = 0
        withContext(Dispatchers.IO) {
            uris.forEach { uri ->
                runCatching {
                    val document = DocumentFile.fromSingleUri(context, uri)
                    val name = document?.name ?: queryName(resolver, uri) ?: "model"
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (extension in supportedExtensions) {
                        val safeName = "${System.currentTimeMillis()}_${name.replace("\\s+".toRegex(), "_")}"
                        val destFile = File(modelsDir, safeName)
                        resolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        imported++
                    }
                }
            }
        }
        refresh()
        return imported
    }

    private suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val entries = modelsDir.listFiles()?.mapNotNull { file ->
                val extension = file.extension.lowercase()
                if (file.isFile && extension in supportedExtensions) {
                    LocalModelEntry(
                        displayName = file.nameWithoutExtension,
                        fileName = file.name,
                        extension = extension,
                        webPath = "https://appassets.androidplatform.net/models/${file.name}"
                    )
                } else null
            }?.sortedByDescending { it.fileName } ?: emptyList()
            _models.value = entries
        }
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String? {
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return null
    }
}
