package com.podbike.app.ui.dashboard.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.podbike.app.R

class DashboardTurnIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_accent)
        style = Paint.Style.FILL
    }
    private val leftPath = Path()
    private val rightPath = Path()
    private var isLeftTurnIndicator = false
    private var isRightTurnIndicator = false
    private var leftAnimator: ObjectAnimator? = null
    private var rightAnimator: ObjectAnimator? = null

    private var leftAlpha = 0f
    private var rightAlpha = 0f

    fun setTurnIndicators(left: Boolean, right: Boolean) {
        isLeftTurnIndicator = left
        isRightTurnIndicator = right
        startAnimations()
        invalidate()
    }

    private fun startAnimations() {
        if (isLeftTurnIndicator) {
            leftAnimator = ObjectAnimator.ofFloat(this, "leftAlpha", 0f, 1f).apply {
                duration = 150
                start()
            }
        } else {
            leftAnimator?.cancel()
            leftAnimator = ObjectAnimator.ofFloat(this, "leftAlpha", 1f, 0f).apply {
                duration = 150
                start()
            }
        }

        if (isRightTurnIndicator) {
            rightAnimator = ObjectAnimator.ofFloat(this, "rightAlpha", 0f, 1f).apply {
                duration = 150
                start()
            }
        } else {
            rightAnimator?.cancel()
            rightAnimator = ObjectAnimator.ofFloat(this, "rightAlpha", 1f, 0f).apply {
                duration = 150
                start()
            }
        }
    }

    fun setLeftAlpha(alpha: Float) {
        leftAlpha = alpha
        invalidate()
    }

    fun setRightAlpha(alpha: Float) {
        rightAlpha = alpha
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val padding = resources.getDimension(R.dimen.padding32)
        val topMargin = resources.getDimension(R.dimen.turn_indicator_top_margin)
        val bottomMargin = resources.getDimension(R.dimen.turn_indicator_bottom_margin)

        paint.alpha = (leftAlpha * 255).toInt()
        if (isLeftTurnIndicator) {
            leftPath.reset()
            leftPath.moveTo(padding, height / 2 + topMargin)
            leftPath.lineTo(width / 2, topMargin)
            leftPath.lineTo(width / 2, height - bottomMargin)
            leftPath.close()
            canvas.drawPath(leftPath, paint)
        }

        paint.alpha = (rightAlpha * 255).toInt()
        if (isRightTurnIndicator) {
            rightPath.reset()
            rightPath.moveTo(width - padding, height / 2 + topMargin)
            rightPath.lineTo(width / 2, topMargin)
            rightPath.lineTo(width / 2, height - bottomMargin)
            rightPath.close()
            canvas.drawPath(rightPath, paint)
        }
    }
}