package com.podbike.app.ui.privacy_policy

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.podbike.app.R

class PrivacyPolicyPagerAdapter :
    RecyclerView.Adapter<PrivacyPolicyPagerAdapter.PrivacyPolicyViewHolder>() {

    private val layouts = listOf(
        R.layout.privacy_policy_privacy_page,
        R.layout.privacy_policy_tos_page
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivacyPolicyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return PrivacyPolicyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrivacyPolicyViewHolder, position: Int) {
        when (position) {
            0 -> {
                holder.itemView.findViewById<TextView>(R.id.privacy_policy_text).run {
                    text = getPrivacyPolicyText(context)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

            1 -> {
                holder.itemView.findViewById<TextView>(R.id.terms_of_conditions_text).run {
                    text =
                        Html.fromHtml(
                            holder.itemView.context.getString(R.string.TermsAndConditionsHtml),
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

        }
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = layouts[position]

    class PrivacyPolicyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun getPrivacyPolicyText(context: Context): SpannableStringBuilder {
        val privacyPolicyText = context.getString(R.string.PrivacyPolicyHtml)
        val spannedHtml: Spanned = Html.fromHtml(privacyPolicyText, Html.FROM_HTML_MODE_LEGACY)

        val additionalText = "For further information on the terms and conditions, please click "
        val hyperlinkText = "here"
        val hyperlinkUrl = "https://www.podbike.com/podbike-terms-conditions/"

        val spannableStringBuilder = SpannableStringBuilder(spannedHtml).apply {
            append("\n")
            append(additionalText)
            val start = length
            append(hyperlinkText)
            setSpan(
                HyperlinkSpan(hyperlinkUrl, context),
                start,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannableStringBuilder
    }
}