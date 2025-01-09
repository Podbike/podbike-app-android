package com.podbike.app.ui.dashboard

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.BuildConfig
import com.podbike.app.data.DistanceUnit
import com.podbike.app.data.DistanceUnit.*
import com.podbike.app.data.SpeedUnit
import com.podbike.app.data.TemperatureUnit
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.model.PodbikeDevice
import com.podbike.app.data.bluetooth.model.PodbikeLightStatus
import com.podbike.app.data.bluetooth.utils.YModem.YModemHelper
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.base.collectWithErrorHandling
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardAction
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardEffect
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardState
import com.podbike.app.utils.UnitConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber.Forest.i
import javax.inject.Inject
import kotlin.time.TimeSource

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val userPreferences: UserPreferences,
    private val unitConverter: UnitConverter
) : StateViewModel<DashboardState, DashboardAction, DashboardEffect>(DashboardState()) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class DashboardTimeoutError(error: Throwable) : ErrorTypeSealed(error)
        class ConnectToDeviceError(error: Throwable) : ErrorTypeSealed(error)
        class FrikarUpdateFailed(error: Throwable) : ErrorTypeSealed(error)
    }

    private var dashboardJob: Job? = null
    private val distanceUnit: DistanceUnit
        get() = userPreferences.getDistanceUnit()

    private val rangeUnit: DistanceUnit
        get() = userPreferences.getDistanceUnit().let {
            when (it) {
                KILOMETERS -> KILOMETERS
                METERS -> KILOMETERS
                MILES -> MILES
            }
        }

    private val speedUnit: SpeedUnit
        get() = userPreferences.getSpeedUnit()

    private val temperatureUnit: TemperatureUnit
        get() = userPreferences.getTemperatureUnit()

    init {
        listenToConnectionChanges()
    }

    @SuppressLint("MissingPermission")
    private fun dashboard() {
        setupDefaultUnits()
        dashboardJob = viewModelScope.launch {
            val device = bluetoothManager.selectedDevice
            launch { device?.data?.battery?.collectWithErrorHandling { updateDeviceDataState(battery = it) } }
            launch {
                device?.data?.speed?.collectWithErrorHandling {
                    updateDeviceDataState(
                        speed = unitConverter.convertSpeed(
                            it.toFloat(),
                            speedUnit,
                            0
                        ),
                        isMoving = if (BuildConfig.DEV) {
                            (uiState.value.deviceData?.assist ?: 0).div(20) >= 3f
                        } else {
                            it.toFloat() >= 3f
                        }
                    )
                }
            }
            launch {
                device?.data?.distance?.collectWithErrorHandling {
                    updateDeviceDataState(
                        distance = unitConverter.convertDistance(
                            it,
                            distanceUnit,
                            1
                        ),
                    )
                }
            }
            launch { device?.data?.assist?.collectWithErrorHandling { updateDeviceDataState(assist = it) } }
            launch { device?.data?.cadence?.collectWithErrorHandling { updateDeviceDataState(cadence = it) } }
            launch {
                device?.data?.lightStatus?.collectWithErrorHandling {
                    updateDeviceDataState(
                        lightStatus = it
                    )
                }
            }
            launch {
                device?.data?.range?.collectWithErrorHandling {
                    updateDeviceDataState(
                        range = unitConverter.convertDistance(
                            it * 1000f,
                            rangeUnit,
                            0
                        )
                    )
                }
            }
            launch {
                device?.data?.temperature?.collectWithErrorHandling {
                    updateDeviceDataState(
                        temperature = it
                    )
                }
            }
        }
    }

    private fun listenToConnectionChanges() {
        viewModelScope.launch {
            val device = bluetoothManager.selectedDevice
            device?.isConnected()?.distinctUntilChanged()
                ?.collectWithErrorHandling { isConnected ->
                    updateState { copy(isConnected = isConnected) }
                    updateTripStartOffset(device, isConnected)
                    if (!isConnected) {
                        bluetoothManager.connect(
                            device.device,
                        )
                    } else {
                        updateState { copy(deviceData = null) }
                    }
                }
        }

    }

    private fun setupDefaultUnits() {
        updateState {
            copy(
                distanceAbbreviation = unitConverter.getDistanceUnitAbbreviation(distanceUnit),
                rangeAbbreviation = unitConverter.getDistanceUnitAbbreviation(rangeUnit),
                speedAbbreviation = unitConverter.getSpeedUnitAbbreviation(speedUnit),
                temperatureAbbreviation = unitConverter.getTemperatureUnitAbbreviation(
                    temperatureUnit
                ),
                freezingTemperature = when (temperatureUnit) {
                    TemperatureUnit.CELSIUS -> 4
                    TemperatureUnit.FAHRENHEIT -> 39
                }
            )
        }
    }

    private fun runFirmwareUpdateCheck() {
        val currentDevice = userPreferences.getMostRecentDevice()
        val hasUpdateStarted = currentDevice?.updateStarted == true
        if (!hasUpdateStarted) {
            return
        }
        updateState { copy(isUpdateOngoing = true) }
        val expectedConfigHash = currentDevice.updateConfigHash
        viewModelScope.launch {
            validateFirmwareUpdate(expectedConfigHash)
        }
    }

    private fun updateTripStartOffset(device: PodbikeDevice, isConnected: Boolean) {
        if (isConnected) {
            device.data.cancelCleanTripDataTimer()
        } else {
            device.data.startCleanTripDataTimer()
        }
        val tripInactivityStartTime = device.data.tripInactivityStartTime
        val tripStartTimeOffset = device.data.tripStartTimeOffset
        if (tripInactivityStartTime != null && isConnected) {
            val inactivityTime = tripInactivityStartTime.elapsedNow()
            device.data.tripStartTimeOffset =
                tripStartTimeOffset?.plus(inactivityTime) ?: tripStartTimeOffset
            device.data.tripInactivityStartTime = null
        }

        if (!isConnected && tripInactivityStartTime == null) {
            device.data.tripInactivityStartTime = TimeSource.Monotonic.markNow()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun validateFirmwareUpdate(expectedConfigHash: Int?) {
        val currentDevice = bluetoothManager.selectedDevice ?: return
        val metadata = try {
            YModemHelper.getDeviceMetadata(currentDevice)
        } catch (e: Exception) {
            i("Failed to get metadata: $e")
            null
        }
        if (metadata == null || expectedConfigHash == null) {
            userPreferences.setUpdateStartedFlag(false, null)
            return updateState {
                copy(
                    isUpdateOngoing = false,
                    error = ErrorTypeSealed.FrikarUpdateFailed(Throwable("Couldn't compare hash"))
                )
            }
        }
        if (metadata.hashCode() == expectedConfigHash) {
            updateState { copy(isUpdateOngoing = false) }
            sendEffect(DashboardEffect.FrikarUpdated)
        } else {
            userPreferences.setUpdateStartedFlag(false, null)
            updateState {
                copy(
                    isUpdateOngoing = false,
                    error = ErrorTypeSealed.FrikarUpdateFailed(Throwable("Metadata hash mismatch"))
                )
            }
        }
        userPreferences.setUpdateStartedFlag(false)
    }

    private fun updateDeviceDataState(
        speed: String? = null,
        battery: Int? = null,
        distance: String? = null,
        assist: Int? = null,
        cadence: Int? = null,
        lightStatus: PodbikeLightStatus? = null,
        range: String? = null,
        temperature: Float? = null,
        isMoving: Boolean? = null
    ) {
        val cadence = cadence ?: uiState.value.deviceData?.cadence ?: 0
        val isMoving = isMoving ?: uiState.value.deviceData?.isMoving ?: false
        val shouldForceDisableInteractiveTutorial = isMoving
        val isInteractiveTutorialEnabled = if (shouldForceDisableInteractiveTutorial) {
            false
        } else {
            uiState.value.isInteractiveTutorialEnabled
        }
        updateState {
            copy(
                deviceData = DeviceDataUiModel(
                    speed = speed ?: this.deviceData?.speed ?: "0",
                    battery = battery ?: this.deviceData?.battery ?: 0,
                    distance = distance ?: this.deviceData?.distance ?: "0",
                    assist = assist ?: this.deviceData?.assist ?: 0,
                    cadence = cadence,
                    isFreezing = (temperature ?: this.deviceData?.temperature ?: 0f) < 4f,
                    lightStatus = lightStatus ?: this.deviceData?.lightStatus
                    ?: PodbikeLightStatus(),
                    time = 0,
                    range = range ?: this.deviceData?.range ?: "0",
                    temperature = temperature ?: this.deviceData?.temperature ?: 0f,
                    isMoving = isMoving,
                    name = bluetoothManager.selectedDevice?.device?.name,
                ),
                distanceAbbreviation = unitConverter.getDistanceUnitAbbreviation(distanceUnit),
                rangeAbbreviation = unitConverter.getDistanceUnitAbbreviation(rangeUnit),
                speedAbbreviation = unitConverter.getSpeedUnitAbbreviation(speedUnit),
                temperatureAbbreviation = unitConverter.getTemperatureUnitAbbreviation(
                    temperatureUnit
                ),
                isInteractiveTutorialEnabled = isInteractiveTutorialEnabled
            )
        }
    }

    override fun processAction(action: DashboardAction) {
        when (action) {

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

            is DashboardAction.EnableInteractiveTutorial -> {
                updateState { copy(isInteractiveTutorialEnabled = true) }
            }

            is DashboardAction.PermissionsChanged -> {
                runFirmwareUpdateCheck()
                val hasAllPermissions =
                    action.hasBluetoothPermissions && action.isBluetoothEnabled && action.isLocationEnabled
                val needToReconnect =
                    action.isBluetoothEnabled && uiState.value.isBluetoothEnabled.not()

                updateState {
                    copy(
                        hasBluetoothPermissions = action.hasBluetoothPermissions,
                        isBluetoothEnabled = action.isBluetoothEnabled,
                        isLocationEnabled = action.isLocationEnabled
                    )
                }

                if (hasAllPermissions) {
                    if (dashboardJob?.isActive != true) {
                        dashboard()
                    } else if (needToReconnect) {
                        viewModelScope.launch {
                            bluetoothManager.selectedDevice?.let {
                                bluetoothManager.selectedDevice = null
                                bluetoothManager.connect(it.device)
                                dashboardJob?.cancel()
                                dashboard()
                            }
                        }
                    }
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

            is DashboardAction.ReturnToDashboardClicked -> {
                updateState { copy(isInteractiveTutorialEnabled = false) }
                sendEffect(DashboardEffect.HideNavigationIcons)
            }

            is DashboardAction.Retry -> {
                updateState { copy(error = null) }
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
        val isInteractiveTutorialEnabled: Boolean = false,
        val isConnected: Boolean = false,
        val deviceData: DeviceDataUiModel? = null,
        val distanceAbbreviation: String = "",
        val rangeAbbreviation: String = "",
        val speedAbbreviation: String = "",
        val temperatureAbbreviation: String = "",
        val freezingTemperature: Int = 0,
        val isUpdateOngoing: Boolean = false,
        val error: ErrorTypeSealed? = null
    ) : UiState

    sealed class DashboardAction : UiAction {
        data object BluetoothPermissions : DashboardAction()
        data object LocationPermissions : DashboardAction()
        data object EnableBluetooth : DashboardAction()
        data object EnableLocation : DashboardAction()
        data object EnableInteractiveTutorial : DashboardAction()
        data class PermissionsChanged(
            val hasBluetoothPermissions: Boolean = false,
            val isBluetoothEnabled: Boolean = false,
            val isLocationEnabled: Boolean = false,
        ) : DashboardAction()

        data object StatisticsClicked : DashboardAction()
        data object SettingsClicked : DashboardAction()
        data object HelpClicked : DashboardAction()
        data object ReturnToDashboardClicked : DashboardAction()
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
        data object HideNavigationIcons : DashboardEffect()
        data object FrikarUpdated : DashboardEffect()
    }

}