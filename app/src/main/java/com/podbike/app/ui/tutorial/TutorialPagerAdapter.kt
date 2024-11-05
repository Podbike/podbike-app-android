package com.podbike.app.ui.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.podbike.app.R

class TutorialPagerAdapter : RecyclerView.Adapter<TutorialPagerAdapter.TutorialViewHolder>() {

    private val layouts = listOf(
        R.layout.tutorial_page_one
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return TutorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        // No binding needed as layouts are static
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = layouts[position]

    class TutorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}