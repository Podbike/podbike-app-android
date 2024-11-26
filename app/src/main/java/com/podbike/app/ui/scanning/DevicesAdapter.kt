package com.podbike.app.ui.scanning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.podbike.app.databinding.ItemDeviceBinding

class DevicesAdapter(
    private val listener: OnDeviceClickListener
) : ListAdapter<DeviceInfo, DevicesAdapter.DeviceViewHolder>(DeviceInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device, listener)
    }

    class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: DeviceInfo, listener: OnDeviceClickListener) {
            binding.deviceName.text = device.name
            binding.deviceAddress.text = device.address
            binding.root.setOnClickListener {
                listener.onDeviceClick(device)
            }
        }
    }
}

class DeviceInfoDiffCallback : DiffUtil.ItemCallback<DeviceInfo>() {
    override fun areItemsTheSame(oldItem: DeviceInfo, newItem: DeviceInfo): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: DeviceInfo, newItem: DeviceInfo): Boolean {
        return oldItem == newItem
    }
}