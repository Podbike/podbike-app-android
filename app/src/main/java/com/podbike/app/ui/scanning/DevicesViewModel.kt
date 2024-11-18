package com.podbike.app.ui.scanning

import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.ui.base.StateViewModel
import javax.inject.Inject

class DevicesViewModel @Inject constructor() :
    StateViewModel<DevicesViewModel.DevicesState, DevicesViewModel.DevicesAction, DevicesViewModel.DevicesEffect>(
        DevicesState()
    ) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class LoadDevicesTimeoutError(error: Throwable) : ErrorTypeSealed(error)
        class ConnectToDeviceError(error: Throwable) : ErrorTypeSealed(error)
    }


    override fun processAction(action: DevicesAction) {
        when (action) {
            is DevicesAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
            }

            is DevicesAction.GoBack -> {
                sendEffect(DevicesEffect.NavigateBack)
            }
        }
    }

    data class DevicesState(
        val isScanning: Boolean = true,

        val isLoading: Boolean = true,
        val error: DevicesViewModel.ErrorTypeSealed? = null
    ) : UiState

    sealed class DevicesAction : UiAction {
        data object Retry : DevicesAction()
        data object GoBack : DevicesAction()
    }

    sealed class DevicesEffect : UiEffect {
        data object NavigateBack : DevicesEffect()
    }

}