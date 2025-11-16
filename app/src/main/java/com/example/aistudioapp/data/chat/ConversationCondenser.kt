package com.example.aistudioapp.data.chat

import com.example.aistudioapp.data.model.ChatMessage
import com.example.aistudioapp.data.model.ChatRole
import java.time.Instant

class ConversationCondenser(
    private val maxContextMessages: Int = 20
) {

    fun condense(messages: List<ChatMessage>): List<ChatMessage> {
        if (messages.size <= maxContextMessages) {
            return messages
        }

        val summaryBuilder = StringBuilder()
        summaryBuilder.appendLine("Summary of earlier conversation:")

        messages.dropLast(maxContextMessages).groupBy { it.role }.forEach { (role, grouped) ->
            val flag = when (role) {
                ChatRole.USER -> "User"
                ChatRole.ASSISTANT -> "Assistant"
                ChatRole.SYSTEM -> "System"
            }
            val representative = grouped.joinToString(separator = " ") { it.content }
            summaryBuilder.appendLine("- $flag context: ${representative.take(600)}")
        }

        val summaryMessage = ChatMessage(
            role = ChatRole.SYSTEM,
            content = summaryBuilder.toString().trim(),
            createdAt = Instant.now().toEpochMilli()
        )

        val recent = messages.takeLast(maxContextMessages).toMutableList()
        recent.add(0, summaryMessage)
        return recent
    }
}
