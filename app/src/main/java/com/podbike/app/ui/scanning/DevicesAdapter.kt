/*
 * Copyright (C) 2026 Phal AS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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