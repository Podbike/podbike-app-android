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

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.podbike.app.R

class DashboardHazardIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.hazard_lights_red)
        style = Paint.Style.FILL
    }
    private val hazardDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_material_hazard_lights)
    private var isHazardIndicator = false
    private var hazardAnimator: ObjectAnimator? = null

    fun setHazardIndicator(isHazard: Boolean) {
        if (isHazard == isHazardIndicator) return
        isHazardIndicator = isHazard
        println("isHazardIndicator: $isHazard")
        startAnimation()
        invalidate()
    }

    private fun startAnimation() {
        hazardAnimator?.cancel()
        if (isHazardIndicator) {
            hazardAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
                duration = 150
                start()
            }
        } else {

            hazardAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.0f).apply {
                duration = 150
                start()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // draw hazardDrawable at the center of the view
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        hazardDrawable?.setBounds(
            (width / 2 - hazardDrawable.intrinsicWidth / 2),
            (height / 2 - hazardDrawable.intrinsicHeight / 2),
            (width / 2 + hazardDrawable.intrinsicWidth / 2),
            (height / 2 + hazardDrawable.intrinsicHeight / 2)
        )
        hazardDrawable?.draw(canvas)
    }
}