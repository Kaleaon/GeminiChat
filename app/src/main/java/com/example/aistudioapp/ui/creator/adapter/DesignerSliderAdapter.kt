package com.example.aistudioapp.ui.creator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aistudioapp.databinding.ItemDesignerSliderBinding
import com.example.aistudioapp.ui.creator.model.DesignerSliderSpec
import com.google.android.material.slider.Slider

class DesignerSliderAdapter(
    private val onValueChanged: (DesignerSliderSpec, Float) -> Unit
) : RecyclerView.Adapter<DesignerSliderAdapter.SliderViewHolder>() {

    private val items = mutableListOf<DesignerSliderSpec>()

    fun submitList(newItems: List<DesignerSliderSpec>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemDesignerSliderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SliderViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class SliderViewHolder(
        private val binding: ItemDesignerSliderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentListener: Slider.OnChangeListener? = null

        fun bind(spec: DesignerSliderSpec) {
            binding.sliderLabel.text = spec.label
            binding.slider.apply {
                valueFrom = spec.min
                valueTo = spec.max
                stepSize = if (spec.step > 0f) spec.step else 0f
                clearOnChangeListeners()
                value = spec.defaultValue.coerceIn(spec.min, spec.max)
                currentListener = Slider.OnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        onValueChanged(spec, value)
                    }
                }
                addOnChangeListener(currentListener!!)
            }
        }
    }
}
