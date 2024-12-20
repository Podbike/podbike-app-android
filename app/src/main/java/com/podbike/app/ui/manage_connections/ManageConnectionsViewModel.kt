package com.podbike.app.ui.manage_connections

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.manage_connections.ManageConnectionsViewModel.ManageConnectionsAction
import com.podbike.app.ui.manage_connections.ManageConnectionsViewModel.ManageConnectionsEffect
import com.podbike.app.ui.manage_connections.ManageConnectionsViewModel.ManageConnectionsState
import com.podbike.app.ui.scanning.DeviceInfo
import com.podbike.app.ui.scanning.DeviceItem
import com.podbike.app.ui.scanning.toDeviceItem
import com.podbike.app.utils.runWithErrorHandling
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ManageConnectionsViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val userPreferences: UserPreferences
) : StateViewModel<ManageConnectionsState, ManageConnectionsAction, ManageConnectionsEffect>(
    ManageConnectionsState()
) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class LoadManageConnectionsTimeoutError(error: Throwable) : ErrorTypeSealed(error)
        class ConnectToDeviceError(error: Throwable) : ErrorTypeSealed(error)
    }

    private var connectJob: Job? = null
    private var currentlyConnectedDevice: DeviceInfo? = null
        get() = bluetoothManager.selectedDevice?.device

    init {
        loadManageConnections()
    }

    private fun loadManageConnections() {
        userPreferences.getRecentDevices().let { recentManageConnections ->
            updateState { copy(manageConnections = recentManageConnections.map { it.toDeviceItem(it.address == currentlyConnectedDevice?.address) }) }
        }
    }

    override fun processAction(action: ManageConnectionsAction) {
        when (action) {
            is ManageConnectionsAction.ToggleScan -> {
                sendEffect(ManageConnectionsEffect.NavigateToScanning)
            }

            is ManageConnectionsAction.BluetoothPermissions -> {
                sendEffect(ManageConnectionsEffect.NavigateToBluetoothPermissions)
            }

            is ManageConnectionsAction.LocationPermissions -> {
                sendEffect(ManageConnectionsEffect.NavigateToLocationPermissions)
            }

            is ManageConnectionsAction.EnableBluetooth -> {
                sendEffect(ManageConnectionsEffect.NavigateToBluetoothSettings)
            }

            is ManageConnectionsAction.EnableLocation -> {
                sendEffect(ManageConnectionsEffect.NavigateToLocationSettings)
            }

            is ManageConnectionsAction.DeviceClick -> {
                connectJob = viewModelScope.launch {
                    sendEffect(ManageConnectionsEffect.ConnectingToDevice(action.deviceItem.name))
                    runWithErrorHandling {
                        val deviceInfo =
                            DeviceInfo(action.deviceItem.name, action.deviceItem.address)
                        val result = bluetoothManager.connect(
                            deviceInfo,
                        )
                        Timber.d("Connect result: $result")
                        result?.let {
                            userPreferences.addRecentDevice(deviceInfo)
                            sendEffect(ManageConnectionsEffect.ConnectToDevice)
                        }
                    }
                }
            }

            is ManageConnectionsAction.CancelConnect -> {
                connectJob?.cancel()
            }

            is ManageConnectionsAction.PermissionsChanged -> {
                val hasAllPermissions =
                    action.hasBluetoothPermissions && action.isBluetoothEnabled && action.isLocationEnabled

                updateState {
                    copy(
                        hasBluetoothPermissions = action.hasBluetoothPermissions,
                        isBluetoothEnabled = action.isBluetoothEnabled,
                        isLocationEnabled = action.isLocationEnabled,
                        manageConnections = if (hasAllPermissions) manageConnections else emptyList()
                    )
                }
            }

            is ManageConnectionsAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
            }

            is ManageConnectionsAction.GoBack -> {
                sendEffect(ManageConnectionsEffect.NavigateBack)
            }
        }
    }

    data class ManageConnectionsState(
        val hasBluetoothPermissions: Boolean = false,
        val isBluetoothEnabled: Boolean = false,
        val isLocationEnabled: Boolean = false,
        val isLoading: Boolean = true,
        val manageConnections: List<DeviceItem> = emptyList(),
        val error: ErrorTypeSealed? = null
    ) : UiState

    sealed class ManageConnectionsAction : UiAction {
        data object ToggleScan : ManageConnectionsAction()
        data object BluetoothPermissions : ManageConnectionsAction()
        data object LocationPermissions : ManageConnectionsAction()
        data object EnableBluetooth : ManageConnectionsAction()
        data object EnableLocation : ManageConnectionsAction()
        data object CancelConnect : ManageConnectionsAction()
        data class DeviceClick(val deviceItem: DeviceItem) : ManageConnectionsAction()
        data class PermissionsChanged(
            val hasBluetoothPermissions: Boolean = false,
            val isBluetoothEnabled: Boolean = false,
            val isLocationEnabled: Boolean = false,
        ) : ManageConnectionsAction()

        data object Retry : ManageConnectionsAction()
        data object GoBack : ManageConnectionsAction()
    }

    sealed class ManageConnectionsEffect : UiEffect {
        data object NavigateBack : ManageConnectionsEffect()
        data object NavigateToBluetoothPermissions : ManageConnectionsEffect()
        data object NavigateToLocationPermissions : ManageConnectionsEffect()
        data object NavigateToBluetoothSettings : ManageConnectionsEffect()
        data object NavigateToLocationSettings : ManageConnectionsEffect()
        data object NavigateToScanning : ManageConnectionsEffect()
        data object ConnectToDevice : ManageConnectionsEffect()
        data class ConnectingToDevice(val deviceName: String) : ManageConnectionsEffect()
    }

}