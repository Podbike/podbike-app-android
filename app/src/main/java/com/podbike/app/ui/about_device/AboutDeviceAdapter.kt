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