package com.podbike.app.ui.firmware_update

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.podbike.app.R
import com.podbike.app.databinding.FragmentFirmwareUpdateBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.ErrorTypeSealed
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.FirmwareUpdateEffect
import com.podbike.app.ui.firmware_update.FirmwareVersion.*
import com.podbike.app.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class FirmwareUpdateFragment : BaseFragment() {

    private lateinit var binding: FragmentFirmwareUpdateBinding
    val viewModel: FirmwareUpdateViewModel by viewModels()

    @Inject
    lateinit var permissionManager: PermissionManager

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            permissions[Manifest.permission.BLUETOOTH] == true &&
                    permissions[Manifest.permission.BLUETOOTH_ADMIN] == true
        }
        val hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        viewModel.processAction(
            FirmwareUpdateViewModel.FirmwareUpdateAction.PermissionsChanged(
                hasBluetoothPermissions = hasBluetoothPermission,
                isBluetoothEnabled = permissionManager.isBluetoothEnabled(),
                isLocationEnabled = permissionManager.isLocationEnabled()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirmwareUpdateBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgePaddings()
        }
        permissionManager.initializePermissionLauncher(requestPermissionsLauncher)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        setupBindings()

        binding.appBar.appBarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.appBarTitle.text = getString(R.string.UpdatePageTitle)

    }

    override fun onResume() {
        super.onResume()
        validatePermissions()
    }

    private fun subscribeToViewModel() {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::processUiState)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.uiEffect
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::processEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupBindings() {
        with(binding) {
            actionPositiveButton.setOnClickListener {
                viewModel.processAction(
                    FirmwareUpdateViewModel.FirmwareUpdateAction.ForwardAction(
                        permissionManager.isBluetoothEnabled()
                    )
                )
            }
            actionNegativeButton.setOnClickListener {
                viewModel.processAction(FirmwareUpdateViewModel.FirmwareUpdateAction.GoBack)
            }
        }
    }

    private fun processUiState(state: FirmwareUpdateViewModel.FirmwareUpdateState) {
        with(binding) {

            var headerTextRes: Int?
            var bodyText: String?
            var centerText: String?
            var positiveButtonTextRes: Int?
            var isCancelButtonVisible: Boolean
            var isProgressVisible: Boolean
            var isBackButtonVisible: Boolean
            when (state.firmwareVersion) {
                UP_TO_DATE -> {
                    headerTextRes = null
                    bodyText = null
                    centerText = getString(R.string.UpdateCurrent)
                    positiveButtonTextRes = R.string.UpdateButtonCheck
                    isCancelButtonVisible = false
                    isProgressVisible = false
                    isBackButtonVisible = false
                }

                UPDATE_AVAILABLE -> {
                    headerTextRes = null
                    bodyText = null
                    centerText = getString(R.string.UpdateAvailable)
                    positiveButtonTextRes = R.string.UpdateButtonGet
                    isCancelButtonVisible = false
                    isProgressVisible = false
                }

                LICENSE_AGREEMENT -> {
                    headerTextRes = R.string.UpdateLicense
                    bodyText = state.firmwareLicense
                    centerText = null
                    positiveButtonTextRes = R.string.UpdateButtonTransfer
                    isCancelButtonVisible = true
                    isProgressVisible = false
                }

                TRANSFER_STARTED -> {
                    headerTextRes = R.string.UpdateTransfer
                    bodyText = getString(R.string.UpdateTransferInfo)
                    centerText = null
                    positiveButtonTextRes = null
                    isCancelButtonVisible = false
                    isProgressVisible = true
                }

                TRANSFER_COMPLETED -> {
                    headerTextRes = null
                    bodyText = null
                    centerText = getString(R.string.UpdateTransferComplete)
                    positiveButtonTextRes = R.string.UpdateButtonUpgrade
                    isCancelButtonVisible = true
                    isProgressVisible = false
                }

                UPGRADE -> {
                    headerTextRes = R.string.UpdateUpgrade
                    bodyText =
                        getString(R.string.UpdateUpgradeInfo1) + "\n" + getString(R.string.UpdateUpgradeInfo2)
                    centerText = null
                    positiveButtonTextRes = null
                    isCancelButtonVisible = false
                    isProgressVisible = true
                }

                UNKNOWN -> {
                    headerTextRes = null
                    bodyText = null
                    centerText = null
                    positiveButtonTextRes = null
                    isCancelButtonVisible = false
                    isProgressVisible = true
                }

                ERROR -> {
                    showErrorDialog(state.error, state.hasBluetoothPermissions)
                    headerTextRes = null
                    bodyText = null
                    centerText = null
                    positiveButtonTextRes = R.string.UpdateButtonCheck
                    isCancelButtonVisible = false
                    isProgressVisible = false
                }
            }

            header.text = headerTextRes?.let { getString(it) }
            body.text = bodyText
            center.text = centerText
            actionPositiveButton.text = positiveButtonTextRes?.let { getString(it) }

            header.isVisible = headerTextRes != null
            body.isVisible = bodyText != null
            center.isVisible = centerText != null
            actionPositiveButton.isVisible = positiveButtonTextRes != null
            actionNegativeButton.isInvisible = !isCancelButtonVisible
            progressBar.isVisible = isProgressVisible
        }
    }

    private fun showErrorDialog(
        error: ErrorTypeSealed?,
        hasBluetoothPermissions: Boolean
    ) {
        val throwable = error?.error

        if (error is ErrorTypeSealed.NoBluetoothDeviceError) {
            showBluetoothDisabledDialog()
        } else {
            when (throwable) {
                is java.net.UnknownHostException -> {
                    showNoInternetDialog()
                }

                is java.net.SocketTimeoutException -> {
                    showNoInternetDialog()
                }

                else -> {
                    showGenericErrorDialog(throwable)
                }
            }
        }
    }

    private fun showBluetoothDisabledDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.UpdateMissingDevice))
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack(R.id.settingsFragment, false)
            }
            .create()
            .show()
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("The Internet connection appears to be offline.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showGenericErrorDialog(throwable: Throwable?) {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.UpdateIssue))
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack(R.id.settingsFragment, false)
            }
            .create()
            .show()
    }

    private fun processEffect(effect: FirmwareUpdateEffect) {
        when (effect) {
            FirmwareUpdateEffect.NavigateBack -> findNavController().popBackStack(
                R.id.settingsFragment,
                false
            )

            FirmwareUpdateEffect.NavigateToBluetoothPermissions -> permissionManager.requestPermissions()
            FirmwareUpdateEffect.NavigateToBluetoothSettings -> enableBluetooth()
            FirmwareUpdateEffect.NavigateToLocationPermissions -> permissionManager.requestPermissions()
            FirmwareUpdateEffect.NavigateToLocationSettings -> intentManager.openLocationSettings()
        }
    }

    private fun enableBluetooth() {
        try {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(requireActivity(), enableBtIntent, 0, null)
        } catch (e: Exception) {
            intentManager.openBluetoothSettings()
        }
    }

    private fun validatePermissions() {
        if (!permissionManager.hasBluetoothPermission() || !permissionManager.isBluetoothEnabled() || !permissionManager.isLocationEnabled()) {
            permissionManager.requestPermissions()
        } else {
            viewModel.processAction(
                FirmwareUpdateViewModel.FirmwareUpdateAction.PermissionsChanged(
                    hasBluetoothPermissions = permissionManager.hasBluetoothPermission(),
                    isBluetoothEnabled = permissionManager.isBluetoothEnabled(),
                    isLocationEnabled = permissionManager.isLocationEnabled()
                )
            )
        }
    }

}