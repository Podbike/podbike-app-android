package com.podbike.app.ui.dashboard.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.podbike.app.R
import com.podbike.app.databinding.ViewCustomProgressBarBinding

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCustomProgressBarBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewCustomProgressBarBinding.inflate(inflater, this, true)
        setProgress(0, "")
        binding.progressBarIndicatorView.setBackgroundColor(Color.GRAY)
        binding.progressBarBackgroundView.setBackgroundColor(Color.GRAY)
    }

    fun setProgress(progress: Int, progressText: String) {
        val backgroundProgress = 100 - progress

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)

        constraintSet.constrainPercentWidth(
            binding.progressBarIndicatorView.id,
            progress / 100f
        )
        constraintSet.constrainPercentWidth(
            binding.progressBarBackgroundView.id,
            backgroundProgress / 100f
        )

        constraintSet.applyTo(binding.root)

        if (progress <= 30) {
            binding.progressBarIndicatorView.setBackgroundColor(Color.RED)
        } else {
            binding.progressBarIndicatorView.setBackgroundResource(R.drawable.gradient_battery_progress)
        }
        binding.progressBarTextView.text = progressText
    }

    // not used for now
    private fun showBatteryStatusAlert(progress: Int) {
        if (progress == 10) {
            alertBatteryStatus("Battery Critical")
        } else if (progress == 30) {
            alertBatteryStatus("Battery Low")
        }
    }

    private fun alertBatteryStatus(text: String) {
        binding.progressBarIndicatorView.isVisible = false
        binding.progressBarBackgroundView.setBackgroundColor(Color.RED)
        binding.progressBarTextView.text = text
        binding.progressBarTextView.isVisible = true

        postDelayed({
            binding.progressBarIndicatorView.isVisible = true
            binding.progressBarBackgroundView.setBackgroundColor(Color.GRAY)
            binding.progressBarTextView.isVisible = false
        }, 2000)
    }
}