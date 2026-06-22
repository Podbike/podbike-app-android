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
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.podbike.app.R
import com.podbike.app.databinding.ViewAssistanceMeterBinding

class AssistanceMeter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAssistanceMeterBinding

    var currentAssistance: Int = 0
        set(value) {
            field = value
            adjustAssistanceLevel(value)
        }

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewAssistanceMeterBinding.inflate(inflater, this)

        context.withStyledAttributes(attrs, R.styleable.AssistanceMeter) {
            currentAssistance = getInt(R.styleable.AssistanceMeter_currentAssistance, 0)
        }
    }

    private fun adjustAssistanceLevel(level: Int) {
        val stages = listOf(0, 20, 40, 60, 80, 100)
        val activeStages = stages.indexOfFirst { it >= level }
        for (i in 0 until activeStages) {
            binding.assistanceRow.getChildAt(i)?.setBackgroundColor(Color.parseColor("#44D62C"))
        }
        for (j in activeStages until stages.size) {
            binding.assistanceRow.getChildAt(j)?.setBackgroundColor(Color.parseColor("#C4C4C4"))
        }
    }
}