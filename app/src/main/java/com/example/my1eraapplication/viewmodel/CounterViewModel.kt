package com.example.my1eraapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.my1eraapplication.model.CounterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CounterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    fun increment() {
        _uiState.update { currentState ->
            val newCount = currentState.count + currentState.step
            currentState.copy(
                count = newCount,
                history = currentState.history + newCount
            )
        }
    }

    fun decrement() {
        _uiState.update { currentState ->
            val newCount = currentState.count - currentState.step
            currentState.copy(
                count = newCount,
                history = currentState.history + newCount
            )
        }
    }

    fun reset() {
        _uiState.update { currentState ->
            currentState.copy(
                count = 0,
                history = currentState.history + 0
            )
        }
    }

    fun setStep(step: Int) {
        if (step > 0) {
            _uiState.update { currentState ->
                currentState.copy(step = step)
            }
        }
    }
}
