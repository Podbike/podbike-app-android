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

package com.podbike.app.ui.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.podbike.app.R

class TutorialPagerAdapter(
    private val onNextClick: () -> Unit,
    private val onPreviousClick: () -> Unit,
    private val onExitClick: () -> Unit
) : RecyclerView.Adapter<TutorialPagerAdapter.TutorialViewHolder>() {

    private val layouts = listOf(
        R.layout.tutorial_page_one,
        R.layout.tutorial_page_two,
        R.layout.tutorial_page_three,
        R.layout.tutorial_page_four
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return TutorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.itemView.findViewById<View>(R.id.tutorial_navigation_next_button)?.setOnClickListener {
            onNextClick()
        }
        holder.itemView.findViewById<View>(R.id.tutorial_navigation_previous_button)?.setOnClickListener {
            onPreviousClick()
        }
        holder.itemView.findViewById<View>(R.id.tutorial_navigation_exit_button)?.setOnClickListener {
            onExitClick()
        }
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = layouts[position]

    class TutorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}