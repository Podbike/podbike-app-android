package com.podbike.app.ui.statistics

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.podbike.app.R

class StatisticView : ConstraintLayout {

    constructor(
        context: Context,
        name: String,
        unit: String,
        value: String,
    ) : super(context) {
        init(context, name, unit, value)

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, null, null, null)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, null, null, null)
    }

    private fun init(context: Context, name: String?, unit: String?, value: String?) {
        LayoutInflater.from(context).inflate(R.layout.item_statistic, this, true)
        name?.let { setStatisticName(it) }
        unit?.let { setStatisticUnit(it) }
        value?.let { setStatisticValue(it) }
    }

    private fun setStatisticName(name: String) {
        findViewById<TextView>(R.id.statistic_name).text = name
    }

    private fun setStatisticUnit(unit: String) {
        findViewById<TextView>(R.id.statistic_unit).text = unit
    }

    private fun setStatisticValue(value: String) {
        findViewById<TextView>(R.id.statistic_value).text = value
    }
}