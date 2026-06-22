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

package com.podbike.app.ui.dashboard.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.podbike.app.R
import com.podbike.app.ui.base.margin

class TooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleTextView: TextView
    private val messageTextView: TextView
    private val okButton: Button
    private val tooltipLayout: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tooltip, this, true)
        titleTextView = findViewById(R.id.tooltipTitle)
        messageTextView = findViewById(R.id.tooltipMessage)
        okButton = findViewById(R.id.tooltipButton)
        tooltipLayout = findViewById(R.id.tooltipLayout)
    }

    fun setTitle(title: String) {
        titleTextView.text = title
    }

    fun getTitle(): String {
        return titleTextView.text.toString()
    }

    fun setMessage(message: String) {
        messageTextView.text = message
    }

    fun showOkButton(show: Boolean, onClickListener: OnClickListener? = null) {
        okButton.visibility = if (show) VISIBLE else GONE
        okButton.setOnClickListener(onClickListener)
        tooltipLayout.margin(left = if (show) 32f else 0f, right = if (show) 32f else 0f)
    }
}