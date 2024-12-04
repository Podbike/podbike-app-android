package com.podbike.app.ui.devices

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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.podbike.app.R
import com.podbike.app.databinding.FragmentDevicesBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import com.podbike.app.ui.scanning.DeviceItem
import com.podbike.app.ui.scanning.DevicesAdapter
import com.podbike.app.ui.scanning.DevicesViewModel
import com.podbike.app.ui.scanning.DevicesViewModel.DevicesEffect
import com.podbike.app.ui.scanning.OnDeviceClickListener
import com.podbike.app.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class DevicesFragment : BaseFragment(), OnDeviceClickListener {

    private lateinit var binding: FragmentDevicesBinding
    val viewModel: DevicesViewModel by viewModels()
    private lateinit var deviceAdapter: DevicesAdapter

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
            DevicesViewModel.DevicesAction.PermissionsChanged(
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
        binding = FragmentDevicesBinding.inflate(inflater, container, false).apply {
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
        viewModel.processAction(DevicesViewModel.DevicesAction.DeviceClick(deviceItem))
    }

    private fun setupRecyclerView() {
        deviceAdapter = DevicesAdapter(this)
        binding.fragmentDevicesLinearLayout.apply {
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
            fragmentDevicesScanningButton.setOnClickListener {
                viewModel.processAction(DevicesViewModel.DevicesAction.ToggleScan)
            }
        }
    }

    private fun processUiState(state: DevicesViewModel.DevicesState) {
        with(binding) {
            if (state.hasBluetoothPermissions == false) {
                //check if also need location permissions for lower android version
                fragmentDevicesErrorButton.isVisible = true
                fragmentDevicesScanningButton.isVisible = false
                fragmentDevicesErrorButton.text = "Add Bluetooth permissions"
                fragmentDevicesErrorButton.setOnClickListener {
                    viewModel.processAction(DevicesViewModel.DevicesAction.BluetoothPermissions)
                }
            } else if (state.isBluetoothEnabled == false) {
                fragmentDevicesErrorButton.isVisible = true
                fragmentDevicesScanningButton.isVisible = false
                fragmentDevicesErrorButton.text = "Enable Bluetooth"
                fragmentDevicesErrorButton.setOnClickListener {
                    viewModel.processAction(DevicesViewModel.DevicesAction.EnableBluetooth)
                }
            } else if (state.isLocationEnabled == false) {
                fragmentDevicesErrorButton.isVisible = true
                fragmentDevicesScanningButton.isVisible = false
                fragmentDevicesErrorButton.text = "Enable Location"
                fragmentDevicesErrorButton.setOnClickListener {
                    viewModel.processAction(DevicesViewModel.DevicesAction.EnableLocation)
                }
            } else {
                fragmentDevicesErrorButton.isVisible = false
                fragmentDevicesScanningButton.isVisible = true
            }

            deviceAdapter.submitList(state.devices)

            state.isScanning.let { isScanning ->
                val buttonText = if (isScanning) {
                    getString(R.string.DeviceStopScan)
                } else {
                    getString(R.string.DeviceStartScan)
                }
                fragmentDevicesScanningButton.text = buttonText
                fragmentDevicesProgressBar.visibility =
                    if (isScanning) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    private fun processEffect(effect: DevicesEffect) {
        when (effect) {
            DevicesEffect.NavigateBack -> findNavController().popBackStack()
            DevicesEffect.NavigateToBluetoothPermissions -> permissionManager.requestPermissions()
            DevicesEffect.NavigateToBluetoothSettings -> enableBluetooth()
            DevicesEffect.NavigateToLocationPermissions -> permissionManager.requestPermissions()
            DevicesEffect.NavigateToLocationSettings -> intentManager.openLocationSettings()
            DevicesEffect.ConnectToDevice -> {
                val navOptions = navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
                findNavController().navigate(
                    R.id.action_devicesFragment_to_dashboardFragment,
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
                DevicesViewModel.DevicesAction.PermissionsChanged(
                    hasBluetoothPermissions = permissionManager.hasBluetoothPermission(),
                    isBluetoothEnabled = permissionManager.isBluetoothEnabled(),
                    isLocationEnabled = permissionManager.isLocationEnabled()
                )
            )
        }
    }

}