package com.example.my1eraapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.my1eraapplication.model.AppThemeMode
import com.example.my1eraapplication.model.ThemeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ThemeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { currentState ->
            currentState.copy(themeMode = mode)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isDynamicColorEnabled = enabled)
        }
    }

    fun toggleThemeMode() {
        _uiState.update { currentState ->
            val nextMode = when (currentState.themeMode) {
                AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                AppThemeMode.LIGHT -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.SYSTEM
            }
            currentState.copy(themeMode = nextMode)
        }
    }
}
