package com.example.aistudioapp.ui.creator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aistudioapp.databinding.ItemModelEntryBinding
import com.example.aistudioapp.ui.creator.model.LocalModelEntry

class ModelLibraryAdapter(
    private val onLoadClicked: (LocalModelEntry) -> Unit
) : RecyclerView.Adapter<ModelLibraryAdapter.ModelViewHolder>() {

    private val entries = mutableListOf<LocalModelEntry>()

    fun submitList(newEntries: List<LocalModelEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val binding = ItemModelEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModelViewHolder(binding)
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    inner class ModelViewHolder(
        private val binding: ItemModelEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LocalModelEntry) {
            binding.modelName.text = entry.displayName
            binding.modelFormat.text = entry.extension.uppercase()
            binding.loadButton.setOnClickListener {
                onLoadClicked(entry)
            }
        }
    }
}
