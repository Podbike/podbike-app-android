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