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

package com.podbike.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class StateViewModel<S : UiState, A : UiAction, E : UiEffect>(initState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initState)
    val uiState: StateFlow<S> = _uiState

    private val _uiEffect = MutableSharedFlow<E>()
    val uiEffect: Flow<E> = _uiEffect

    abstract fun processAction(action: A)

    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }

    protected fun updateState(stateBlock: S.() -> S) {
        _uiState.value = stateBlock(_uiState.value)
    }

    protected fun handleError(error: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(error)
    }
}
