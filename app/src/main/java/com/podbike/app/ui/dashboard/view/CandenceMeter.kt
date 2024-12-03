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