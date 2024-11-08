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