package com.podbike.app.ui.statistics

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.DistanceUnit
import com.podbike.app.data.SpeedUnit
import com.podbike.app.data.TemperatureUnit
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.dashboard.DashboardViewModel.ErrorTypeSealed
import com.podbike.app.ui.statistics.StatisticsViewModel.StatisticsAction
import com.podbike.app.ui.statistics.StatisticsViewModel.StatisticsEffect
import com.podbike.app.ui.statistics.StatisticsViewModel.StatisticsState
import com.podbike.app.utils.UnitConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("NAME_SHADOWING")
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val userPreferences: UserPreferences,
    private val unitConverter: UnitConverter
) : StateViewModel<StatisticsState, StatisticsAction, StatisticsEffect>(StatisticsState()) {

    private var statisticsJob: Job? = null
    private val distanceUnit: DistanceUnit
        get() = userPreferences.getDistanceUnit()

    private val speedUnit: SpeedUnit
        get() = userPreferences.getSpeedUnit()

    private val temperatureUnit: TemperatureUnit
        get() = userPreferences.getTemperatureUnit()


    fun statistics() {
        try {
            statisticsJob = viewModelScope.launch {
                val device = bluetoothManager.selectedDevice
                launch { device?.data?.temperature?.collect { updateStatisticsState(temperature = it) } }
                launch { device?.data?.distance?.collect { updateStatisticsState(distance = it) } }
                launch { device?.data?.averageSpeed?.collect { updateStatisticsState(averageSpeed = it) } }
                launch { device?.data?.averageRpm?.collect { updateStatisticsState(averageRpm = it) } }
                launch {
                    device?.data?.powerGenerated?.collect {
                        updateStatisticsState(
                            powerGenerated = it
                        )
                    }
                }
                launch { device?.data?.battery?.collect { updateStatisticsState(battery = it) } }
                launch { device?.data?.maxSpeed?.collect { updateStatisticsState(maxSpeed = it) } }
            }
        } catch (e: Exception) {
            println("Error fetching statistics: $e")
        }
    }

    override fun processAction(action: StatisticsAction) {
        when (action) {
            StatisticsAction.GoBack -> {}
        }
    }

    private fun updateStatisticsState(
        distance: Float? = null,
        temperature: Float? = null,
        averageSpeed: Float? = null,
        averageRpm: Int? = null,
        powerGenerated: Int? = null,
        battery: Int? = null,
        maxSpeed: Int? = null
    ) {
        val co2Saved = distance?.let { (it / 1000) * (0.1204 - 0.00044) }?.toFloat()
        val averageSpeed =
            averageSpeed?.let { unitConverter.convertSpeed(it, speedUnit, 1) }
        val distance =
            distance?.let { unitConverter.convertDistance(it, distanceUnit, 1) }
        val temperature =
            temperature?.let { unitConverter.convertTemperature(it, temperatureUnit) }
        val maxSpeed = maxSpeed?.let {
            unitConverter.convertSpeed(it.toFloat(), speedUnit, 1)
        }

        updateState {
            copy(
                data = StatisticsDataUiModel(
                    interiorTemperature = temperature ?: uiState.value.data.interiorTemperature,
                    co2Saved = co2Saved ?: uiState.value.data.co2Saved,
                    totalDistance = distance ?: uiState.value.data.totalDistance,
                    currentTripTime = 0,
                    averageSpeed = averageSpeed ?: uiState.value.data.averageSpeed,
                    averageRpm = averageRpm ?: uiState.value.data.averageRpm,
                    powerGenerated = powerGenerated ?: uiState.value.data.powerGenerated,
                    batteryRemaining = battery ?: uiState.value.data.batteryRemaining,
                    distanceUnit = unitConverter.getDistanceUnitAbbreviation(distanceUnit),
                    speedUnit = unitConverter.getSpeedUnitAbbreviation(speedUnit),
                    temperatureUnit = unitConverter.getTemperatureUnitAbbreviation(
                        temperatureUnit
                    ),
                    maxSpeed = maxSpeed ?: uiState.value.data.maxSpeed
                )
            )
        }
    }


    data class StatisticsState(
        val data: StatisticsDataUiModel = StatisticsDataUiModel.empty,
        val error: ErrorTypeSealed? = null
    ) : UiState

    sealed class StatisticsAction : UiAction {
        data object GoBack : StatisticsAction()
    }

    sealed class StatisticsEffect : UiEffect {

    }
}