package com.podbike.app.ui.manage_connections

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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.podbike.app.R
import com.podbike.app.databinding.FragmentManageConnectionsBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import com.podbike.app.ui.manage_connections.ManageConnectionsViewModel.ManageConnectionsEffect
import com.podbike.app.ui.scanning.DeviceItem
import com.podbike.app.ui.scanning.DevicesAdapter
import com.podbike.app.ui.scanning.OnDeviceClickListener
import com.podbike.app.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class ManageConnectionsFragment : BaseFragment(), OnDeviceClickListener {

    private lateinit var binding: FragmentManageConnectionsBinding
    val viewModel: ManageConnectionsViewModel by viewModels()
    private lateinit var deviceAdapter: DevicesAdapter
    private var connectingDialog: AlertDialog? = null

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
            ManageConnectionsViewModel.ManageConnectionsAction.PermissionsChanged(
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
        binding = FragmentManageConnectionsBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgePaddings()
        }
        permissionManager.initializePermissionLauncher(requestPermissionsLauncher)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeToViewModel()
        setupBindings()

        binding.appBar.appBarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.appBarTitle.text = getString(R.string.DevicesPageTitle)

    }

    override fun onResume() {
        super.onResume()
        validatePermissions()
    }

    override fun onDeviceClick(deviceItem: DeviceItem) {
        viewModel.processAction(
            ManageConnectionsViewModel.ManageConnectionsAction.DeviceClick(
                deviceItem
            )
        )
    }

    private fun setupRecyclerView() {
        deviceAdapter = DevicesAdapter(this)
        binding.fragmentManageConnectionsLinearLayout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deviceAdapter
        }
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
            fragmentManageConnectionsScanningButton.setOnClickListener {
                viewModel.processAction(ManageConnectionsViewModel.ManageConnectionsAction.ToggleScan)
            }
        }
    }

    private fun processUiState(state: ManageConnectionsViewModel.ManageConnectionsState) {
        with(binding) {
            if (state.error != null) {
                connectingDialog?.hide()
            }
            if (state.hasBluetoothPermissions == false) {
                //check if also need location permissions for lower android version
                fragmentManageConnectionsErrorButton.isVisible = true
                fragmentManageConnectionsScanningButton.isVisible = false
                fragmentManageConnectionsErrorButton.text = "Add Bluetooth permissions"
                fragmentManageConnectionsErrorButton.setOnClickListener {
                    viewModel.processAction(ManageConnectionsViewModel.ManageConnectionsAction.BluetoothPermissions)
                }
            } else if (state.isBluetoothEnabled == false) {
                fragmentManageConnectionsErrorButton.isVisible = true
                fragmentManageConnectionsScanningButton.isVisible = false
                fragmentManageConnectionsErrorButton.text = "Enable Bluetooth"
                fragmentManageConnectionsErrorButton.setOnClickListener {
                    viewModel.processAction(ManageConnectionsViewModel.ManageConnectionsAction.EnableBluetooth)
                }
            } else if (state.isLocationEnabled == false) {
                fragmentManageConnectionsErrorButton.isVisible = true
                fragmentManageConnectionsScanningButton.isVisible = false
                fragmentManageConnectionsErrorButton.text = "Enable Location"
                fragmentManageConnectionsErrorButton.setOnClickListener {
                    viewModel.processAction(ManageConnectionsViewModel.ManageConnectionsAction.EnableLocation)
                }
            } else {
                fragmentManageConnectionsErrorButton.isVisible = false
                fragmentManageConnectionsScanningButton.isVisible = true
            }

            deviceAdapter.submitList(state.manageConnections)
        }
    }

    private fun processEffect(effect: ManageConnectionsEffect) {
        when (effect) {
            ManageConnectionsEffect.NavigateBack -> findNavController().popBackStack()
            ManageConnectionsEffect.NavigateToBluetoothPermissions -> permissionManager.requestPermissions()
            ManageConnectionsEffect.NavigateToBluetoothSettings -> enableBluetooth()
            ManageConnectionsEffect.NavigateToLocationPermissions -> permissionManager.requestPermissions()
            ManageConnectionsEffect.NavigateToLocationSettings -> intentManager.openLocationSettings()
            ManageConnectionsEffect.NavigateToScanning -> {
                connectingDialog?.hide()
                findNavController().navigate(R.id.action_manageConnectionsFragment_to_devicesFragment)
            }

            ManageConnectionsEffect.ConnectToDevice -> {
                connectingDialog?.hide()
                findNavController().navigate(R.id.action_manageConnectionsFragment_to_dashboardFragment)
            }

            is ManageConnectionsEffect.ConnectingToDevice -> showConnectingDialog(effect.deviceName)
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
                ManageConnectionsViewModel.ManageConnectionsAction.PermissionsChanged(
                    hasBluetoothPermissions = permissionManager.hasBluetoothPermission(),
                    isBluetoothEnabled = permissionManager.isBluetoothEnabled(),
                    isLocationEnabled = permissionManager.isLocationEnabled()
                )
            )
        }
    }

    private fun showConnectingDialog(deviceName: String) {
        val message = "${getString(R.string.ConnectingTo)} $deviceName"
        connectingDialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setMessage(message)
            .setPositiveButton(getString(R.string.Cancel)) { _, _ ->
                connectingDialog?.dismiss()
                viewModel.processAction(ManageConnectionsViewModel.ManageConnectionsAction.CancelConnect)
            }
            .setCancelable(false)
            .create()
        connectingDialog?.show()
    }

}