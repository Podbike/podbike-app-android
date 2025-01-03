package com.podbike.app.ui.base

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.podbike.app.R
import com.podbike.app.databinding.AppBarBinding
import com.podbike.app.ui.navigation.IntentManager
import javax.inject.Inject

open class BaseFragment : Fragment() {

    @Inject
    lateinit var intentManager: IntentManager

    fun bindNavigation(titleStringRes: Int, appBar: AppBarBinding) {
        appBar.appBarBack.setOnClickListener {
            findNavController().popBackStack()
        }
        appBar.appBarTitle.text = getString(titleStringRes)
    }

    fun showAlertDialog(message: String) {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

}