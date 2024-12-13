package com.podbike.app.ui.scanning

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.manager.ConnectionManager
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.base.collectWithErrorHandling
import com.podbike.app.ui.scanning.DevicesViewModel.DevicesAction
import com.podbike.app.ui.scanning.DevicesViewModel.DevicesEffect
import com.podbike.app.ui.scanning.DevicesViewModel.DevicesState
import com.podbike.app.utils.runWithErrorHandling
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val userPreferences: UserPreferences
) : StateViewModel<DevicesState, DevicesAction, DevicesEffect>(DevicesState()) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class LoadDevicesTimeoutError(error: Throwable) : ErrorTypeSealed(error)
        class ConnectToDeviceError(error: Throwable) : ErrorTypeSealed(error)
    }

    private var loadDevicesJob: Job? = null
    private var currentlyConnectedDevice: DeviceInfo? = null
        get() = bluetoothManager.selectedDevice?.device

    private fun loadDevices() {
        userPreferences.getRecentDevices().let { recentDevices ->
            updateState { copy(devices = recentDevices.map { it.toDeviceItem(it.address == currentlyConnectedDevice?.address) }) }
        }
        loadDevicesJob = viewModelScope.launch {
            bluetoothManager.scan().collectWithErrorHandling { deviceList ->
                val updatedDevices =
                    (uiState.value.devices + deviceList.map { it.toDeviceItem(it.address == currentlyConnectedDevice?.address) }).distinctBy { it.address }
                updateState { copy(isLoading = false, devices = updatedDevices) }
            }
        }
    }

    override fun processAction(action: DevicesAction) {
        when (action) {
            is DevicesAction.ToggleScan -> {
                if (uiState.value.isScanning) {
                    loadDevicesJob?.cancel()
                    updateState { copy(isScanning = false) }
                } else {
                    updateState { copy(isScanning = true, devices = listOf()) }
                    loadDevices()
                }
            }

            is DevicesAction.BluetoothPermissions -> {
                sendEffect(DevicesEffect.NavigateToBluetoothPermissions)
            }

            is DevicesAction.LocationPermissions -> {
                sendEffect(DevicesEffect.NavigateToLocationPermissions)
            }

            is DevicesAction.EnableBluetooth -> {
                sendEffect(DevicesEffect.NavigateToBluetoothSettings)
            }

            is DevicesAction.EnableLocation -> {
                sendEffect(DevicesEffect.NavigateToLocationSettings)
            }

            is DevicesAction.DeviceClick -> {
                viewModelScope.launch {
                    sendEffect(DevicesEffect.ConnectingToDevice(action.deviceItem.name))
                    runWithErrorHandling {
                        val deviceInfo =
                            DeviceInfo(action.deviceItem.name, action.deviceItem.address)
                        val result = bluetoothManager.connect(
                            deviceInfo,
                        )
                        Timber.d("Connect result: $result")
                        result?.let {
                            userPreferences.addRecentDevice(deviceInfo)
                            sendEffect(DevicesEffect.ConnectToDevice)
                        }
                    }
                }
            }

            is DevicesAction.PermissionsChanged -> {
                val hasAllPermissions =
                    action.hasBluetoothPermissions && action.isBluetoothEnabled && action.isLocationEnabled

                updateState {
                    copy(
                        hasBluetoothPermissions = action.hasBluetoothPermissions,
                        isBluetoothEnabled = action.isBluetoothEnabled,
                        isLocationEnabled = action.isLocationEnabled,
                        isScanning = hasAllPermissions,
                        devices = if (hasAllPermissions) devices else emptyList()
                    )
                }
                if (!hasAllPermissions) {
                    loadDevicesJob?.cancel()
                } else if (loadDevicesJob?.isActive != true) {
                    loadDevices()
                }
            }

            is DevicesAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
            }

            is DevicesAction.GoBack -> {
                sendEffect(DevicesEffect.NavigateBack)
            }
        }
    }

    data class DevicesState(
        val isScanning: Boolean = false,
        val hasBluetoothPermissions: Boolean = false,
        val isBluetoothEnabled: Boolean = false,
        val isLocationEnabled: Boolean = false,
        val isLoading: Boolean = true,
        val devices: List<DeviceItem> = emptyList(),
        val error: ErrorTypeSealed? = null
    ) : UiState

    sealed class DevicesAction : UiAction {
        data object ToggleScan : DevicesAction()
        data object BluetoothPermissions : DevicesAction()
        data object LocationPermissions : DevicesAction()
        data object EnableBluetooth : DevicesAction()
        data object EnableLocation : DevicesAction()
        data class DeviceClick(val deviceItem: DeviceItem) : DevicesAction()
        data class PermissionsChanged(
            val hasBluetoothPermissions: Boolean = false,
            val isBluetoothEnabled: Boolean = false,
            val isLocationEnabled: Boolean = false,
        ) : DevicesAction()

        data object Retry : DevicesAction()
        data object GoBack : DevicesAction()
    }

    sealed class DevicesEffect : UiEffect {
        data object NavigateBack : DevicesEffect()
        data object NavigateToBluetoothPermissions : DevicesEffect()
        data object NavigateToLocationPermissions : DevicesEffect()
        data object NavigateToBluetoothSettings : DevicesEffect()
        data object NavigateToLocationSettings : DevicesEffect()
        data object ConnectToDevice : DevicesEffect()
        data class ConnectingToDevice(val deviceName: String) : DevicesEffect()
    }

}