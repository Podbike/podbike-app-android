package com.podbike.app.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.podbike.app.R

class SettingView : ConstraintLayout {

    constructor(
        context: Context,
        name: String,
        onClick: () -> Unit = {}
    ) : super(context) {
        init(context, name, onClick)

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

    private fun init(context: Context, name: String?, onClick: () -> Unit = {}) {
        LayoutInflater.from(context).inflate(R.layout.item_setting, this, true)
        name?.let { setSettingName(it) }
        setOnClickListener { onClick() }
    }

    fun setSettingName(name: String) {
        findViewById<TextView>(R.id.setting_name).text = name
    }
}