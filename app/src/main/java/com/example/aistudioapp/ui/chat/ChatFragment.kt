package com.example.aistudioapp.ui.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aistudioapp.MainActivity
import com.example.aistudioapp.data.model.AttachmentMetadata
import com.example.aistudioapp.data.model.AttachmentType
import com.example.aistudioapp.databinding.FragmentChatBinding
import com.example.aistudioapp.di.ServiceLocator
import com.example.aistudioapp.ui.common.SimpleTextWatcher
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModel.Factory(ServiceLocator.provideChatRepository(requireContext()))
    }

    private lateinit var adapter: ChatMessageAdapter

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        handleAttachments(uris, AttachmentType.IMAGE)
    }

    private val pickDocumentsLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        handleAttachments(uris, AttachmentType.DOCUMENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupInputControls()
        observeUiState()
        observeNewChatRequest()
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter { attachment ->
            Snackbar.make(binding.root, attachment.displayName, Snackbar.LENGTH_SHORT).show()
        }
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@ChatFragment.adapter
        }
    }

    private fun setupInputControls() {
        binding.messageInput.addTextChangedListener(
            SimpleTextWatcher { text -> viewModel.updateInput(text) }
        )

        binding.messageInputLayout.setEndIconOnClickListener { viewModel.sendMessage() }
        binding.sendButton.setOnClickListener { viewModel.sendMessage() }
        binding.addImageButton.setOnClickListener {
            pickImagesLauncher.launch(arrayOf("image/*"))
        }
        binding.addDocumentButton.setOnClickListener {
            pickDocumentsLauncher.launch(
                arrayOf(
                    "application/pdf",
                    "text/plain",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
            )
        }
        binding.condenseButton.setOnClickListener {
            viewModel.condenseCurrentThread()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.messages) {
                        if (state.messages.isNotEmpty()) {
                            binding.chatRecyclerView.scrollToPosition(state.messages.size - 1)
                        }
                    }
                    if (binding.messageInput.text?.toString() != state.inputText) {
                        binding.messageInput.setText(state.inputText)
                        binding.messageInput.setSelection(state.inputText.length)
                    }
                    binding.messageInputLayout.isEndIconVisible = !state.isSending
                    binding.sendButton.isEnabled = !state.isSending
                    binding.threadTitle.text = state.availableThreads.firstOrNull {
                        it.id == state.currentThreadId
                    }?.title ?: getString(com.example.aistudioapp.R.string.default_thread_title)

                    val providerLabel = state.availableThreads.firstOrNull { it.id == state.currentThreadId }
                        ?.providerType
                        ?.let { getString(it.displayNameRes) }
                        ?: getString(com.example.aistudioapp.R.string.provider_gemini)
                    binding.providerLabel.text = getString(
                        com.example.aistudioapp.R.string.subtitle_active_provider,
                        providerLabel
                    )

                    updateAttachmentPreview(state.attachments)

                    state.errorMessage?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun observeNewChatRequest() {
        parentFragmentManager.setFragmentResultListener(
            MainActivity.REQUEST_NEW_CHAT,
            viewLifecycleOwner
        ) { _, _ ->
            viewModel.startNewThread()
        }
    }

    private fun updateAttachmentPreview(attachments: List<AttachmentMetadata>) {
        val chipGroup = binding.attachmentGroup
        chipGroup.removeAllViews()
        if (attachments.isEmpty()) {
            chipGroup.visibility = View.GONE
            return
        }
        chipGroup.visibility = View.VISIBLE
        attachments.forEach { attachment ->
            val chip = Chip(requireContext()).apply {
                text = attachment.displayName
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    viewModel.removeAttachment(attachment.uri)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun handleAttachments(uris: List<Uri>, type: AttachmentType) {
        val resolver = requireContext().contentResolver
        uris.forEach { uri ->
            runCatching {
                resolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            buildAttachmentMetadata(uri, type)?.let { metadata ->
                viewModel.addAttachment(metadata)
            }
        }
    }

    private fun buildAttachmentMetadata(uri: Uri, type: AttachmentType): AttachmentMetadata? {
        val resolver = requireContext().contentResolver
        var name = uri.lastPathSegment ?: "attachment"
        var size: Long? = null
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) name = cursor.getString(nameIndex)
                if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
            }
        }
        val mimeType = resolver.getType(uri) ?: when (type) {
            AttachmentType.IMAGE -> "image/*"
            AttachmentType.DOCUMENT -> "application/octet-stream"
        }
        return AttachmentMetadata(
            uri = uri.toString(),
            displayName = name,
            mimeType = mimeType,
            type = type,
            sizeInBytes = size
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
