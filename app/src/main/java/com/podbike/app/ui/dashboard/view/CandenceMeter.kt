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
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.podbike.app.R
import com.podbike.app.databinding.ViewCadenceMeterBinding

class CadenceMeter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCadenceMeterBinding

    var currentCadence: Int = 0
        set(value) {
            field = value
            adjustCadenceLevel(value)
        }

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewCadenceMeterBinding.inflate(inflater, this)

        context.withStyledAttributes(attrs, R.styleable.CadenceMeter) {
            currentCadence = getInt(R.styleable.CadenceMeter_currentCadence, 0)
        }
    }

    private fun adjustCadenceLevel(level: Int) {
        for (i in 0 until level) {
            binding.cadenceRow.getChildAt(i)
                ?.setBackgroundResource(R.drawable.cadence_green_background)
        }
        for (j in level until 10) {
            binding.cadenceRow.getChildAt(j)
                ?.setBackgroundResource(R.drawable.cadence_gray_background)
        }
    }
}