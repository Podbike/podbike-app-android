package com.podbike.app.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.wrapper.PodbikeDevice
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardAction
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardEffect
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager
) : StateViewModel<DashboardState, DashboardAction, DashboardEffect>(DashboardState()) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class DashboardTimeoutError(error: Throwable) : ErrorTypeSealed(error)
        class ConnectToDeviceError(error: Throwable) : ErrorTypeSealed(error)
    }

    private var dashboardJob: Job? = null

    private fun dashboard() {
        dashboardJob = viewModelScope.launch {
            val device = bluetoothManager.selectedDevice
            launch { device?.data?.battery?.collect { updateDeviceDataState(battery = it) } }
            launch { device?.data?.speed?.collect { updateDeviceDataState(speed = it) } }
            launch { device?.data?.distance?.collect { updateDeviceDataState(distance = it) } }
        }
    }

    private fun updateDeviceDataState(
        speed: Int? = null,
        battery: Int? = null,
        distance: Float? = null
    ) {
        updateState {
            copy(
                isLoading = !isLoading,
                deviceData = DeviceDataUiModel(
                    speed = speed ?: this.deviceData?.speed ?: 0,
                    battery = battery ?: this.deviceData?.battery ?: 0,
                    distance = distance ?: this.deviceData?.distance ?: 0f,
                    time = 0,
                )
            )
        }
    }

    override fun processAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.ToggleDashboard -> {
                if (uiState.value.isLoading) {
                    dashboardJob?.cancel()
                    updateState { copy(isLoading = false) }
                } else {
                    updateState { copy(isLoading = true) }
                    dashboard()
                }
            }

            is DashboardAction.BluetoothPermissions -> {
                sendEffect(DashboardEffect.NavigateToBluetoothPermissions)
            }

            is DashboardAction.LocationPermissions -> {
                sendEffect(DashboardEffect.NavigateToLocationPermissions)
            }

            is DashboardAction.EnableBluetooth -> {
                sendEffect(DashboardEffect.NavigateToBluetoothSettings)
            }

            is DashboardAction.EnableLocation -> {
                sendEffect(DashboardEffect.NavigateToLocationSettings)
            }

            is DashboardAction.PermissionsChanged -> {
                val hasAllPermissions =
                    action.hasBluetoothPermissions && action.isBluetoothEnabled && action.isLocationEnabled

                updateState {
                    copy(
                        hasBluetoothPermissions = action.hasBluetoothPermissions,
                        isBluetoothEnabled = action.isBluetoothEnabled,
                        isLocationEnabled = action.isLocationEnabled,
                        isLoading = hasAllPermissions
                    )
                }
                if (!hasAllPermissions) {
                    dashboardJob?.cancel()
                } else if (dashboardJob?.isActive != true) {
                    dashboard()
                }
            }

            is DashboardAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
            }

            is DashboardAction.GoBack -> {
                sendEffect(DashboardEffect.NavigateBack)
            }
        }
    }

    data class DashboardState(
        val hasBluetoothPermissions: Boolean = false,
        val isBluetoothEnabled: Boolean = false,
        val isLocationEnabled: Boolean = false,
        val isLoading: Boolean = true,
        val deviceData: DeviceDataUiModel? = null,
        val error: ErrorTypeSealed? = null
    ) : UiState

    sealed class DashboardAction : UiAction {
        data object ToggleDashboard : DashboardAction()
        data object BluetoothPermissions : DashboardAction()
        data object LocationPermissions : DashboardAction()
        data object EnableBluetooth : DashboardAction()
        data object EnableLocation : DashboardAction()
        data class PermissionsChanged(
            val hasBluetoothPermissions: Boolean = false,
            val isBluetoothEnabled: Boolean = false,
            val isLocationEnabled: Boolean = false,
        ) : DashboardAction()

        data object Retry : DashboardAction()
        data object GoBack : DashboardAction()
    }

    sealed class DashboardEffect : UiEffect {
        data object NavigateBack : DashboardEffect()
        data object NavigateToBluetoothPermissions : DashboardEffect()
        data object NavigateToLocationPermissions : DashboardEffect()
        data object NavigateToBluetoothSettings : DashboardEffect()
        data object NavigateToLocationSettings : DashboardEffect()
    }

}