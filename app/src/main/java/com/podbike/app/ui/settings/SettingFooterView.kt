package com.podbike.app.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.podbike.app.R

class SettingFooterView : ConstraintLayout {

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
        LayoutInflater.from(context).inflate(R.layout.item_setting_footer, this, true)
        name?.let { setSettingName(it) }
    }

    fun setSettingName(name: String) {
        findViewById<TextView>(R.id.setting_name).text = name
    }
}