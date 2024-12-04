package com.podbike.app.ui.scanning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.podbike.app.databinding.ItemDeviceBinding

class DevicesAdapter(
    private val listener: OnDeviceClickListener
) : ListAdapter<DeviceItem, DevicesAdapter.DeviceViewHolder>(DeviceItemDiffCallback()) {

    var currentlyConnectedDevice: DeviceItem? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device, listener)
    }

    class DeviceViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: DeviceItem, listener: OnDeviceClickListener) {
            binding.deviceName.text = device.name
            binding.deviceAddress.text = device.address
            binding.deviceCurrentlyConnected.isVisible = device.isCurrentlyConnected
            binding.root.setOnClickListener {
                listener.onDeviceClick(device)
            }
        }
    }
}

class DeviceItemDiffCallback : DiffUtil.ItemCallback<DeviceItem>() {
    override fun areItemsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: DeviceItem, newItem: DeviceItem): Boolean {
        return oldItem == newItem
    }
}