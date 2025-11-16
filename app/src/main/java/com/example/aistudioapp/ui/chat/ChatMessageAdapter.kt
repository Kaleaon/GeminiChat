package com.example.aistudioapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aistudioapp.data.model.AttachmentMetadata
import com.example.aistudioapp.data.model.ChatMessage
import com.example.aistudioapp.data.model.ChatRole
import com.example.aistudioapp.databinding.ItemMessageAiBinding
import com.example.aistudioapp.databinding.ItemMessageSystemBinding
import com.example.aistudioapp.databinding.ItemMessageUserBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatMessageAdapter(
    private val onAttachmentTapped: (AttachmentMetadata) -> Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).role) {
            ChatRole.USER -> VIEW_USER
            ChatRole.ASSISTANT -> VIEW_ASSISTANT
            ChatRole.SYSTEM -> VIEW_SYSTEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_USER -> {
                val binding = ItemMessageUserBinding.inflate(inflater, parent, false)
                UserViewHolder(binding)
            }
            VIEW_ASSISTANT -> {
                val binding = ItemMessageAiBinding.inflate(inflater, parent, false)
                AssistantViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageSystemBinding.inflate(inflater, parent, false)
                SystemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message, onAttachmentTapped)
            is AssistantViewHolder -> holder.bind(message, onAttachmentTapped)
            is SystemViewHolder -> holder.bind(message)
        }
    }

    private class UserViewHolder(
        private val binding: ItemMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, onAttachmentTapped: (AttachmentMetadata) -> Unit) {
            binding.messageText.text = message.content
            binding.messageTimestamp.text = formatTimestamp(message.createdAt)
            bindAttachments(binding.attachmentChipGroup, message.attachments, onAttachmentTapped)
        }
    }

    private class AssistantViewHolder(
        private val binding: ItemMessageAiBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, onAttachmentTapped: (AttachmentMetadata) -> Unit) {
            binding.messageText.text = message.content
            binding.messageTimestamp.text = formatTimestamp(message.createdAt)
            bindAttachments(binding.attachmentChipGroup, message.attachments, onAttachmentTapped)
        }
    }

    private class SystemViewHolder(
        private val binding: ItemMessageSystemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.messageText.text = message.content
        }
    }

    companion object {
        private const val VIEW_USER = 1
        private const val VIEW_ASSISTANT = 2
        private const val VIEW_SYSTEM = 3

        private val DiffCallback = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
                oldItem.id == newItem.id && oldItem.createdAt == newItem.createdAt

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
                oldItem == newItem
        }

        private val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        private fun formatTimestamp(timestamp: Long): String =
            dateFormat.format(Date(timestamp))

        private fun bindAttachments(
            chipGroup: ChipGroup,
            attachments: List<AttachmentMetadata>,
            onAttachmentTapped: (AttachmentMetadata) -> Unit
        ) {
            chipGroup.removeAllViews()
            if (attachments.isEmpty()) {
                chipGroup.visibility = View.GONE
                return
            }
            chipGroup.visibility = View.VISIBLE
            attachments.forEach { attachment ->
                val chip = Chip(chipGroup.context).apply {
                    text = attachment.displayName
                    isCloseIconVisible = false
                    setOnClickListener { onAttachmentTapped(attachment) }
                }
                chipGroup.addView(chip)
            }
        }
    }
}
