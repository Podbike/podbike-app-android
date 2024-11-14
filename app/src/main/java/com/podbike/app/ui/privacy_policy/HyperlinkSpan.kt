package com.podbike.app.ui.privacy_policy

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.content.Context
import android.content.Intent
import android.net.Uri

class HyperlinkSpan(private val url: String, private val context: Context) : ClickableSpan() {
    override fun onClick(widget: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = true
    }
}