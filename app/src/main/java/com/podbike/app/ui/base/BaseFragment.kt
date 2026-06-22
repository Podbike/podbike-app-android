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

package com.podbike.app.ui.base

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.podbike.app.R
import com.podbike.app.databinding.AppBarBinding
import com.podbike.app.ui.navigation.IntentManager
import timber.log.Timber.Forest.e
import javax.inject.Inject

open class BaseFragment : Fragment() {

    @Inject
    lateinit var intentManager: IntentManager

    var dialog: AlertDialog? = null

    fun bindNavigation(titleStringRes: Int, appBar: AppBarBinding) {
        appBar.appBarBack.setOnClickListener {
            findNavController().popBackStack()
        }
        appBar.appBarTitle.text = getString(titleStringRes)
    }

    fun showAlertDialog(message: String, action: () -> Unit = {}) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog?.dismiss()
                try {
                    action.invoke()
                } catch (e: Exception) {
                    e(e)
                }
            }
            .setCancelable(false)
            .create()

        dialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }
}