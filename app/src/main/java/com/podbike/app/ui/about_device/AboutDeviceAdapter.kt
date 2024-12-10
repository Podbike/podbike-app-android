package com.podbike.app.ui.about_device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.podbike.app.databinding.ItemAboutDeviceBinding

class AboutDeviceAdapter(private val items: List<AboutDeviceItem>) :
    RecyclerView.Adapter<AboutDeviceAdapter.AboutDeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutDeviceViewHolder {
        val binding =
            ItemAboutDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AboutDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AboutDeviceViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class AboutDeviceViewHolder(private val binding: ItemAboutDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AboutDeviceItem) {
            binding.itemName.text = item.name
            binding.itemDivider.isVisible = item.hasDivider
        }
    }
}