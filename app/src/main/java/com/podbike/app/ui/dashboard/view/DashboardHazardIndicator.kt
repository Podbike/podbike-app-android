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
    private var hazardAlpha = 0f

    fun setHazardIndicator(isHazard: Boolean) {
        //println("setHazardIndicator: $isHazard")
        isHazardIndicator = isHazard
        startAnimation()
        invalidate()
    }

    private fun startAnimation() {
        if (isHazardIndicator) {
            hazardAnimator = ObjectAnimator.ofFloat(this, "hazardAlpha", 0f, 1f).apply {
                duration = 500
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        } else {
            hazardAnimator?.cancel()
            hazardAnimator = ObjectAnimator.ofFloat(this, "hazardAlpha", 1f, 0f).apply {
                duration = 500
                start()
            }
        }
    }

    fun setHazardAlpha(alpha: Float) {
        hazardAlpha = alpha
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isHazardIndicator) {
            hazardDrawable?.alpha = (hazardAlpha * 255).toInt()
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
}