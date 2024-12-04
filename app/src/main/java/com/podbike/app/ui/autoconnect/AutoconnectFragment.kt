package com.podbike.app.ui.autoconnect

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.podbike.app.R
import com.podbike.app.databinding.FragmentAutoconnectBinding
import com.podbike.app.ui.autoconnect.AutoconnectViewModel.AutoconnectAction
import com.podbike.app.ui.autoconnect.AutoconnectViewModel.AutoconnectEffect
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgeMargins
import com.podbike.app.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AutoconnectFragment : BaseFragment() {

    private lateinit var binding: FragmentAutoconnectBinding
    val viewModel: AutoconnectViewModel by viewModels()

    @Inject
    lateinit var permissionManager: PermissionManager

    //TODO export to permission manager
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
            AutoconnectAction.PermissionsChanged(
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
        binding = FragmentAutoconnectBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgeMargins()
        }
        permissionManager.initializePermissionLauncher(requestPermissionsLauncher)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        setupBindings()
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
            fragmentAutoconnectConnectButton.setOnClickListener {
                viewModel.processAction(AutoconnectAction.ToggleAutoconnect)
            }
            fragmentAutoconnectScanButton.setOnClickListener {
                viewModel.processAction(AutoconnectAction.ScanForFrikarClick)
            }
        }
    }

    private fun processUiState(state: AutoconnectViewModel.AutoconnectState) {
        with(binding) {
            if (state.isLoading) {
                fragmentAutoconnectProgressBar.visibility = View.VISIBLE
                fragmentAutoconnectProgressText.text = getString(R.string.FrikarConnecting)
                fragmentAutoconnectConnectButton.text = getString(R.string.FrikarPause)
            } else {
                fragmentAutoconnectProgressBar.visibility = View.INVISIBLE
                fragmentAutoconnectProgressText.text = getString(R.string.FrikarConnectionLost)
                fragmentAutoconnectConnectButton.text = getString(R.string.FrikarReconnect)
            }
            if (state.selectedDevice == null) {
                fragmentAutoconnectHeader.text = "${getString(R.string.ConnectingTo)} FRIKAR"
            } else {
                fragmentAutoconnectHeader.text =
                    "${getString(R.string.ConnectingTo)} ${state.selectedDevice.name}"
            }
        }
    }

    private fun processEffect(effect: AutoconnectEffect) {
        when (effect) {
            AutoconnectEffect.NavigateBack -> findNavController().popBackStack()
            AutoconnectEffect.NavigateToBluetoothPermissions -> permissionManager.requestPermissions()
            AutoconnectEffect.NavigateToBluetoothSettings -> enableBluetooth()
            AutoconnectEffect.NavigateToLocationPermissions -> permissionManager.requestPermissions()
            AutoconnectEffect.NavigateToLocationSettings -> intentManager.openLocationSettings()
            AutoconnectEffect.NavigateToDevices -> {
                val navOptions = navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
                findNavController().navigate(
                    R.id.action_autoconnectFragment_to_devicesFragment,
                    null,
                    navOptions
                )
            }

            AutoconnectEffect.AutoconnectToFrikar -> {
                val navOptions = navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
                findNavController().navigate(
                    R.id.action_autoconnectFragment_to_dashboardFragment,
                    null,
                    navOptions
                )
            }
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
                AutoconnectAction.PermissionsChanged(
                    hasBluetoothPermissions = permissionManager.hasBluetoothPermission(),
                    isBluetoothEnabled = permissionManager.isBluetoothEnabled(),
                    isLocationEnabled = permissionManager.isLocationEnabled()
                )
            )
        }
    }

}