package com.podbike.app.ui.dashboard.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.podbike.app.R.*


class DashboardTurnIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, com.podbike.app.R.color.color_accent)
        style = Paint.Style.FILL
    }
    private val background =
        ResourcesCompat.getDrawable(resources, drawable.gradient_background, null)
    private val leftPath = Path()
    private val rightPath = Path()
    private var isLeftTurnIndicator = false
    private var isRightTurnIndicator = false
    private var wasLeft = false
    private var animator: ObjectAnimator? = null

    fun setTurnIndicators(left: Boolean, right: Boolean) {
        if (left == isLeftTurnIndicator && right == isRightTurnIndicator) return
        isLeftTurnIndicator = left
        isRightTurnIndicator = right

        if (left || right) wasLeft = left

        startAnimations(left || right)
        invalidate()
    }

    private fun startAnimations(show: Boolean) {
        animator?.cancel()
        if (show) {
            animator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
                duration = 150
                start()
            }
        } else {
            animator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
                duration = 150
                start()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val padding = resources.getDimension(dimen.padding32)

        background?.setBounds(0, 0, width.toInt(), height.toInt())
        background?.draw(canvas)

        if (wasLeft) {
            leftPath.reset()
            leftPath.moveTo(padding, height / 2)
            leftPath.lineTo(width / 2, (height / 2) + (height / 3))
            leftPath.lineTo(width / 2, (height / 2) - (height / 3))
            leftPath.close()
            canvas.drawPath(leftPath, paint)
        } else {
            rightPath.reset()
            rightPath.moveTo(width - padding, height / 2)
            rightPath.lineTo(width / 2, (height / 2) + (height / 3))
            rightPath.lineTo(width / 2, (height / 2) - (height / 3))
            rightPath.close()
            canvas.drawPath(rightPath, paint)
        }
    }
}