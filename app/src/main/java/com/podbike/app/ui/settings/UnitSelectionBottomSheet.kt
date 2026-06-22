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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.podbike.app.R

class UnitSelectionBottomSheet(
    private val title: String,
    private val options: List<Pair<String, String>>,
    private val currentSelection: String,
    private val onOptionSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_unit_selection, container, false)
        view.findViewById<TextView>(R.id.bottom_sheet_title).text = title

        val optionsContainer = view.findViewById<ViewGroup>(R.id.options_container)
        options.forEach { (enumName, displayName) ->
            val optionView = inflater.inflate(R.layout.item_option, optionsContainer, false) as TextView
            optionView.text = displayName
            if (enumName == currentSelection) {
                optionView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_24, 0)
            }
            optionView.setOnClickListener {
                onOptionSelected(enumName)
                dismiss()
            }
            optionsContainer.addView(optionView)
        }

        return view
    }
}