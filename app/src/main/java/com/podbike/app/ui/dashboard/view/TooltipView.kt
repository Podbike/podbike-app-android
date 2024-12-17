package com.podbike.app.ui.dashboard.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.podbike.app.R

class TooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleTextView: TextView
    private val messageTextView: TextView
    private val okButton: Button

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tooltip, this, true)
        titleTextView = findViewById(R.id.tooltipTitle)
        messageTextView = findViewById(R.id.tooltipMessage)
        okButton = findViewById(R.id.tooltipButton)
    }

    fun setTitle(title: String) {
        titleTextView.text = title
    }

    fun getTitle(): String {
        return titleTextView.text.toString()
    }

    fun setMessage(message: String) {
        messageTextView.text = message
    }

    fun showOkButton(show: Boolean, onClickListener: OnClickListener? = null) {
        okButton.visibility = if (show) VISIBLE else GONE
        okButton.setOnClickListener(onClickListener)
    }
}