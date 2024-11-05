package com.podbike.app.ui.dashboard

import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.ui.base.StateViewModel
import javax.inject.Inject

class DashboardViewModel @Inject constructor() :
    StateViewModel<DashboardViewModel.DashboardState, DashboardViewModel.DashboardAction, DashboardViewModel.DashboardEffect>(
        DashboardState()
    ) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class LoadInitialData(error: Throwable) : ErrorTypeSealed(error)
    }


    override fun processAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
            }

            is DashboardAction.GoBack -> {
                sendEffect(DashboardEffect.NavigateBack)
            }
        }
    }

    data class DashboardState(
        val isLoading: Boolean = true,
        val error: DashboardViewModel.ErrorTypeSealed? = null
    ) : UiState

    sealed class DashboardAction : UiAction {
        data object Retry : DashboardAction()
        data object GoBack : DashboardAction()
    }

    sealed class DashboardEffect : UiEffect {
        data object NavigateBack : DashboardEffect()
    }

}