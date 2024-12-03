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
        color = ContextCompat.getColor(context, R.color.red)
        style = Paint.Style.FILL
    }
    private val hazardDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_material_hazard_lights)
    private var isHazardIndicator = false
    private var hazardAnimator: ObjectAnimator? = null
    private var hazardAlpha = 0f

    fun setHazardIndicator(isHazard: Boolean) {
        isHazardIndicator = isHazard
        startAnimation()
        invalidate()
    }

    private fun startAnimation() {
        if (isHazardIndicator) {
            hazardAnimator = ObjectAnimator.ofFloat(this, "hazardAlpha", 0f, 1f).apply {
                duration = 150
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        } else {
            hazardAnimator?.cancel()
            hazardAnimator = ObjectAnimator.ofFloat(this, "hazardAlpha", 1f, 0f).apply {
                duration = 150
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
            paint.alpha = (hazardAlpha * 255).toInt()
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            hazardDrawable?.alpha = (hazardAlpha * 255).toInt()
            hazardDrawable?.setBounds(64, 64, width - 64, height - 64)
            hazardDrawable?.draw(canvas)
        }
    }
}