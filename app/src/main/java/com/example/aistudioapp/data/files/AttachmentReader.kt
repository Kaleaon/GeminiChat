package com.example.aistudioapp.data.files

import android.content.Context
import android.util.Base64
import com.example.aistudioapp.data.model.AttachmentMetadata
import com.example.aistudioapp.data.model.AttachmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.android.PDFBoxResourceLoader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

class AttachmentReader(private val context: Context) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun extractPayload(attachment: AttachmentMetadata): String = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val inputStream = resolver.openInputStream(attachment.parsedUri)
            ?: error("Unable to open attachment: ${attachment.displayName}")

        inputStream.use { stream ->
            when (attachment.type) {
                AttachmentType.IMAGE -> encodeAsBase64(stream, attachment.mimeType)
                AttachmentType.DOCUMENT -> parseDocument(stream, attachment.mimeType)
            }
        }
    }

    private fun encodeAsBase64(stream: InputStream, mimeType: String): String {
        val bytes = stream.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:$mimeType;base64,$base64"
    }

    private fun parseDocument(stream: InputStream, mimeType: String): String = when {
        mimeType.equals("application/pdf", ignoreCase = true) -> parsePdf(stream)
        mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ignoreCase = true) -> parseDocx(stream)
        mimeType.startsWith("text/", ignoreCase = true) || mimeType.contains("json") -> parseText(stream)
        else -> parseText(stream)
    }

    private fun parsePdf(stream: InputStream): String {
        PDDocument.load(stream).use { doc ->
            val stripper = PDFTextStripper()
            return stripper.getText(doc)
        }
    }

    private fun parseDocx(stream: InputStream): String {
        ZipInputStream(stream).use { zip ->
            var entry = zip.nextEntry
            val sb = StringBuilder()
            while (entry != null) {
                if (!entry.isDirectory && entry.name.equals("word/document.xml", ignoreCase = true)) {
                    val reader = BufferedReader(InputStreamReader(zip))
                    val xml = reader.readText()
                    sb.append(
                        xml.replace("<[^>]+>".toRegex(), " ")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                    )
                    break
                }
                entry = zip.nextEntry
            }
            return sb.toString().trim()
        }
    }

    private fun parseText(stream: InputStream): String =
        BufferedReader(InputStreamReader(stream)).use { it.readText() }

    suspend fun summarizeAttachments(attachments: List<AttachmentMetadata>): Map<String, String> {
        if (attachments.isEmpty()) return emptyMap()
        val summary = linkedMapOf<String, String>()
        attachments.forEach { attachment ->
            runCatching {
                extractPayload(attachment)
            }.onSuccess { payload ->
                summary[attachment.displayName] = payload.take(MAX_PREVIEW_CHAR).trim()
            }.onFailure {
                summary[attachment.displayName] = "[Attachment could not be processed]"
            }
        }
        return summary
    }

    companion object {
        private const val MAX_PREVIEW_CHAR = 6000
    }
}
