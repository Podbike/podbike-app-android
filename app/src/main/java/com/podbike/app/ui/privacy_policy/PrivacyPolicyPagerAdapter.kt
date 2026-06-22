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
                    text = getTermsAndConditionsText(context)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

        }
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = layouts[position]

    class PrivacyPolicyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun getPrivacyPolicyText(context: Context): SpannableStringBuilder {
        val htmlText = context.getString(R.string.PrivacyPolicyHtml)
        val spannedHtml: Spanned = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

        val additionalText = "For further information on the policy, please click "
        val hyperlinkText = "here"
        val hyperlinkUrl = "https://www.podbike.com/privacy-and-cookie-policy/"

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

    private fun getTermsAndConditionsText(context: Context): SpannableStringBuilder {
        val htmlText = context.getString(R.string.TermsAndConditionsHtml)
        val spannedHtml: Spanned = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

        val additionalText = "For further information on the terms and conditions, please click "
        val hyperlinkText = "here"
        val hyperlinkUrl = "https://www.podbike.com/terms-conditions/"

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