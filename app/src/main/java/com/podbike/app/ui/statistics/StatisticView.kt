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