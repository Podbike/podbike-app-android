/*
 * Copyright (C) 2026 Phal AS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.podbike.app.ui.autoconnect

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.manager.ConnectionManager
import com.podbike.app.ui.autoconnect.AutoconnectViewModel.*
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.scanning.DeviceInfo
import com.podbike.app.utils.runWithErrorHandling
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoconnectViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val userPreferences: UserPreferences
) : StateViewModel<AutoconnectState, AutoconnectAction, AutoconnectEffect>(AutoconnectState()) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class AutoconnectTimeoutError(error: Throwable) : ErrorTypeSealed(error)
        class ConnectToDeviceError(error: Throwable) : ErrorTypeSealed(error)
    }

    private var autoconnectJob: Job? = null

    private fun autoconnect() {
        autoconnectJob = viewModelScope.launch {
            val deviceInfo = userPreferences.getMostRecentDevice()
            if (deviceInfo == null) {
                delay(1000)
                updateState {
                    copy(
                        isLoading = false,
                        error = ErrorTypeSealed.ConnectToDeviceError(Throwable("No device found"))
                    )
                }
                return@launch
            } else {
                updateState {
                    copy(
                        selectedDevice = deviceInfo,
                        isLoading = true,
                        error = null
                    )
                }
                delay(1000)
                runWithErrorHandling {
                    bluetoothManager.connect(
                        deviceInfo,
                    )
                }
                delay(1000)
                sendEffect(AutoconnectEffect.AutoconnectToFrikar)
            }
        }
    }

    override fun processAction(action: AutoconnectAction) {
        when (action) {
            is AutoconnectAction.ToggleAutoconnect -> {
                if (uiState.value.isLoading) {
                    autoconnectJob?.cancel()
                    updateState { copy(isLoading = false) }
                } else {
                    updateState { copy(isLoading = true) }
                    autoconnect()
                }
            }

            is AutoconnectAction.BluetoothPermissions -> {
                sendEffect(AutoconnectEffect.NavigateToBluetoothPermissions)
            }

            is AutoconnectAction.LocationPermissions -> {
                sendEffect(AutoconnectEffect.NavigateToLocationPermissions)
            }

            is AutoconnectAction.EnableBluetooth -> {
                sendEffect(AutoconnectEffect.NavigateToBluetoothSettings)
            }

            is AutoconnectAction.EnableLocation -> {
                sendEffect(AutoconnectEffect.NavigateToLocationSettings)
            }

            is AutoconnectAction.ScanForFrikarClick -> {
                updateState { copy(isLoading = false, error = null) }
                sendEffect(AutoconnectEffect.NavigateToDevices)
            }

            is AutoconnectAction.PermissionsChanged -> {
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
                    autoconnectJob?.cancel()
                } else if (autoconnectJob?.isActive != true) {
                    autoconnect()
                }
            }

            is AutoconnectAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
            }

            is AutoconnectAction.GoBack -> {
                sendEffect(AutoconnectEffect.NavigateBack)
            }
        }
    }

    data class AutoconnectState(
        val hasBluetoothPermissions: Boolean = false,
        val isBluetoothEnabled: Boolean = false,
        val isLocationEnabled: Boolean = false,
        val isLoading: Boolean = false,
        val error: ErrorTypeSealed? = null,
        val selectedDevice: DeviceInfo? = null
    ) : UiState

    sealed class AutoconnectAction : UiAction {
        data object ToggleAutoconnect : AutoconnectAction()
        data object BluetoothPermissions : AutoconnectAction()
        data object LocationPermissions : AutoconnectAction()
        data object EnableBluetooth : AutoconnectAction()
        data object EnableLocation : AutoconnectAction()
        data object ScanForFrikarClick : AutoconnectAction()
        data class PermissionsChanged(
            val hasBluetoothPermissions: Boolean = false,
            val isBluetoothEnabled: Boolean = false,
            val isLocationEnabled: Boolean = false,
        ) : AutoconnectAction()

        data object Retry : AutoconnectAction()
        data object GoBack : AutoconnectAction()
    }

    sealed class AutoconnectEffect : UiEffect {
        data object NavigateBack : AutoconnectEffect()
        data object NavigateToBluetoothPermissions : AutoconnectEffect()
        data object NavigateToLocationPermissions : AutoconnectEffect()
        data object NavigateToBluetoothSettings : AutoconnectEffect()
        data object NavigateToLocationSettings : AutoconnectEffect()
        data object NavigateToDevices : AutoconnectEffect()
        data object AutoconnectToFrikar : AutoconnectEffect()
    }

}