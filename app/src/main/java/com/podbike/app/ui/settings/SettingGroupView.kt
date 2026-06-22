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

package com.podbike.app.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.podbike.app.R

class SettingGroupView : ConstraintLayout {

    constructor(context: Context, name: String) : super(context) {
        init(context, name)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, null)
    }

    private fun init(context: Context, name: String?) {
        LayoutInflater.from(context).inflate(R.layout.item_setting_group, this, true)
        name?.let { setSettingName(it) }
    }

    fun setSettingName(name: String) {
        findViewById<TextView>(R.id.setting_group_name).text = name
    }
}