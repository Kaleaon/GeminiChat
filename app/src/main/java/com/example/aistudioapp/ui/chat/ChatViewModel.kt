package com.example.aistudioapp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aistudioapp.data.model.AttachmentMetadata
import com.example.aistudioapp.data.model.ChatMessage
import com.example.aistudioapp.data.model.ChatThread
import com.example.aistudioapp.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var messagesJob: Job? = null

    init {
        observeThreads()
    }

    private fun observeThreads() {
        viewModelScope.launch {
            repository.threads.collectLatest { threads ->
                val current = _uiState.value.currentThreadId
                val nextThreadId = current ?: threads.firstOrNull()?.id
                val previousThreadId = _uiState.value.currentThreadId
                _uiState.update { state ->
                    state.copy(
                        availableThreads = threads,
                        currentThreadId = nextThreadId
                    )
                }
                if (nextThreadId != null && nextThreadId != previousThreadId) {
                    observeMessages(nextThreadId)
                }
            }
        }
    }

    private fun observeMessages(threadId: Long) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.observeMessages(threadId).collectLatest { messages ->
                _uiState.update { state ->
                    state.copy(
                        currentThreadId = threadId,
                        messages = messages,
                        isEmptyState = messages.isEmpty()
                    )
                }
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun addAttachment(metadata: AttachmentMetadata) {
        _uiState.update { state ->
            state.copy(attachments = state.attachments + metadata)
        }
    }

    fun removeAttachment(uri: String) {
        _uiState.update { state ->
            state.copy(attachments = state.attachments.filterNot { it.uri == uri })
        }
    }

    fun startNewThread() {
        messagesJob?.cancel()
        _uiState.update {
            it.copy(
                currentThreadId = null,
                inputText = "",
                attachments = emptyList(),
                messages = emptyList(),
                isEmptyState = true
            )
        }
    }

    fun sendMessage() {
        val prompt = _uiState.value.inputText.trim()
        if (prompt.isBlank()) return
        if (_uiState.value.isSending) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSending = true, errorMessage = null) }
                val result = repository.sendMessage(
                    existingThreadId = _uiState.value.currentThreadId,
                    prompt = prompt,
                    attachments = _uiState.value.attachments
                )
                _uiState.update {
                    it.copy(
                        currentThreadId = result.threadId,
                        inputText = "",
                        attachments = emptyList(),
                        isSending = false
                    )
                }
                observeMessages(result.threadId)
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isSending = false, errorMessage = t.message ?: "Unable to send")
                }
            }
        }
    }

    fun condenseCurrentThread() {
        val threadId = _uiState.value.currentThreadId ?: return
        viewModelScope.launch {
            runCatching {
                repository.condenseThread(threadId)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    data class ChatUiState(
        val availableThreads: List<ChatThread> = emptyList(),
        val currentThreadId: Long? = null,
        val messages: List<ChatMessage> = emptyList(),
        val attachments: List<AttachmentMetadata> = emptyList(),
        val inputText: String = "",
        val isSending: Boolean = false,
        val isEmptyState: Boolean = true,
        val errorMessage: String? = null
    )

    class Factory(private val repository: ChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository) as T
        }
    }
}
