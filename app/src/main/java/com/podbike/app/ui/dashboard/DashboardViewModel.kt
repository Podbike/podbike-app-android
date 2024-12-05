package com.podbike.app.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.DistanceUnit
import com.podbike.app.data.SpeedUnit
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.manager.ConnectionManager
import com.podbike.app.data.bluetooth.model.PodbikeLightStatus
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardAction
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardEffect
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardState
import com.podbike.app.utils.UnitConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val userPreferences: UserPreferences,
    private val unitConverter: UnitConverter
) : StateViewModel<DashboardState, DashboardAction, DashboardEffect>(DashboardState()) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class DashboardTimeoutError(error: Throwable) : ErrorTypeSealed(error)
        class ConnectToDeviceError(error: Throwable) : ErrorTypeSealed(error)
    }

    private var dashboardJob: Job? = null
    private var reconnectJob: Job? = null
    private val distanceUnit: DistanceUnit
        get() = userPreferences.getDistanceUnit()

    private val speedUnit: SpeedUnit
        get() = userPreferences.getSpeedUnit()

    private fun dashboard() {
        dashboardJob = viewModelScope.launch {
            val device = bluetoothManager.selectedDevice
            launch { device?.data?.battery?.collect { updateDeviceDataState(battery = it) } }
            launch {
                device?.data?.speed?.collect {
                    updateDeviceDataState(
                        speed = unitConverter.convertSpeed(
                            it.toFloat(),
                            speedUnit,
                            0
                        )
                    )
                }
            }
            launch {
                device?.data?.distance?.collect {
                    updateDeviceDataState(
                        distance = unitConverter.convertDistance(
                            it,
                            distanceUnit,
                            1
                        ),
                    )
                }
            }
            launch { device?.data?.assist?.collect { updateDeviceDataState(assist = it) } }
            launch { device?.data?.cadence?.collect { updateDeviceDataState(cadence = it) } }
            launch { device?.data?.lightStatus?.collect { updateDeviceDataState(lightStatus = it) } }
            launch {
                device?.data?.range?.collect {
                    updateDeviceDataState(
                        range = unitConverter.convertDistance(
                            it * 1000f,
                            distanceUnit,
                            0
                        )
                    )
                }
            }
            launch { device?.data?.temperature?.collect { updateDeviceDataState(temperature = it) } }
            launch {
                device?.isConnected()?.collect { isConnected ->
                    updateState { copy(isLoading = !isConnected) }
                    if (!isConnected) {
                        reconnectJob = viewModelScope.launch {
                            while (true) {
                                bluetoothManager.connect(
                                    device.device,
                                    coroutineScope = ConnectionManager.connectionScope
                                )
                                dashboard()
                                delay(5000)
                            }
                        }
                    } else {
                        reconnectJob?.cancel()
                    }
                }
            }
        }
    }

    private fun updateDeviceDataState(
        speed: String? = null,
        battery: Int? = null,
        distance: String? = null,
        assist: Int? = null,
        cadence: Int? = null,
        lightStatus: PodbikeLightStatus? = null,
        range: String? = null,
        temperature: Int? = null,
    ) {
        //TODO replace cadence with speed eventually
        val cadence = cadence ?: uiState.value.deviceData?.cadence ?: 0
        val isMoving = if (cadence >= 3) {
            true
        } else if (cadence <= 1) {
            false
        } else {
            uiState.value.deviceData?.isMoving == true
        }
        updateState {
            copy(
                deviceData = DeviceDataUiModel(
                    speed = speed ?: this.deviceData?.speed ?: "0",
                    battery = battery ?: this.deviceData?.battery ?: 0,
                    distance = distance ?: this.deviceData?.distance ?: "0",
                    assist = assist ?: this.deviceData?.assist ?: 0,
                    cadence = cadence,
                    isFreezing = (temperature ?: this.deviceData?.temperature ?: 0) < 4,
                    lightStatus = lightStatus ?: this.deviceData?.lightStatus
                    ?: PodbikeLightStatus(),
                    time = 0,
                    distanceAbbreviation = unitConverter.getDistanceUnitAbbreviation(distanceUnit),
                    range = range ?: this.deviceData?.range ?: "0",
                    temperature = temperature ?: this.deviceData?.temperature ?: 0,
                    isMoving = isMoving,
                    name = bluetoothManager.selectedDevice?.device?.name
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
                        isLocationEnabled = action.isLocationEnabled
                    )
                }
                if (!hasAllPermissions) {
                    dashboardJob?.cancel()
                } else if (dashboardJob?.isActive != true) {
                    dashboard()
                }
            }

            is DashboardAction.SettingsClicked -> {
                sendEffect(DashboardEffect.NavigateToAppSettings)
            }

            is DashboardAction.StatisticsClicked -> {
                sendEffect(DashboardEffect.NavigateToStatistics)
            }

            is DashboardAction.HelpClicked -> {
                sendEffect(DashboardEffect.NavigateToHelp)
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

        data object StatisticsClicked : DashboardAction()
        data object SettingsClicked : DashboardAction()
        data object HelpClicked : DashboardAction()
        data object Retry : DashboardAction()
        data object GoBack : DashboardAction()
    }

    sealed class DashboardEffect : UiEffect {
        data object NavigateBack : DashboardEffect()
        data object NavigateToBluetoothPermissions : DashboardEffect()
        data object NavigateToLocationPermissions : DashboardEffect()
        data object NavigateToBluetoothSettings : DashboardEffect()
        data object NavigateToLocationSettings : DashboardEffect()
        data object NavigateToAppSettings : DashboardEffect()
        data object NavigateToHelp : DashboardEffect()
        data object NavigateToStatistics : DashboardEffect()
    }

}