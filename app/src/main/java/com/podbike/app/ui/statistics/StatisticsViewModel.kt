package com.podbike.app.ui.statistics

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.DistanceUnit
import com.podbike.app.data.SpeedUnit
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


    private fun statistics() {
        statisticsJob = viewModelScope.launch {
            val device = bluetoothManager.selectedDevice
            launch { device?.data?.temperature?.collect { updateStatisticsState(temperature = it) } }
            launch { device?.data?.distance?.collect { updateStatisticsState(distance = it) } }
            launch { device?.data?.averageSpeed?.collect { updateStatisticsState(averageSpeed = it) } }
            launch { device?.data?.averageRpm?.collect { updateStatisticsState(averageRpm = it) } }
            launch { device?.data?.powerGenerated?.collect { updateStatisticsState(powerGenerated = it) } }
            launch { device?.data?.battery?.collect { updateStatisticsState(battery = it) } }
        }
    }

    override fun processAction(action: StatisticsAction) {
        when (action) {
            StatisticsAction.GoBack -> {}
        }
    }

    private fun updateStatisticsState(
        distance: Float? = null,
        temperature: Int? = null,
        averageSpeed: Int? = null,
        averageRpm: Int? = null,
        powerGenerated: Int? = null,
        battery: Int? = null
    ) {
        val co2Saved = distance?.div(5)
        updateState {
            copy(
                data = StatisticsDataUiModel(
                    interiorTemperature = temperature ?: uiState.value.data?.interiorTemperature
                    ?: 0,
                    co2Saved = co2Saved ?: uiState.value.data?.co2Saved ?: 0f,
                    totalDistance = distance ?: uiState.value.data?.totalDistance ?: 0f,
                    currentTripTime = 0,
                    averageSpeed = averageSpeed ?: uiState.value.data?.averageSpeed ?: 0,
                    averageRpm = averageRpm ?: uiState.value.data?.averageRpm ?: 0,
                    powerGenerated = powerGenerated ?: uiState.value.data?.powerGenerated ?: 0,
                    batteryRemaining = battery ?: uiState.value.data?.batteryRemaining ?: 0
                )
            )
        }
    }


    data class StatisticsState(
        val data: StatisticsDataUiModel? = null,
        val error: ErrorTypeSealed? = null
    ) : UiState

    sealed class StatisticsAction : UiAction {
        data object GoBack : StatisticsAction()
    }

    sealed class StatisticsEffect : UiEffect {

    }
}