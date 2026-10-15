package com.example.my1eraapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.my1eraapplication.model.AppDestination
import com.example.my1eraapplication.model.NavigationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

    fun navigateTo(destination: AppDestination) {
        _uiState.update { currentState ->
            if (currentState.currentDestination == destination) {
                currentState
            } else {
                currentState.copy(
                    currentDestination = destination,
                    backStack = currentState.backStack + destination
                )
            }
        }
    }

    fun popBackStack(): Boolean {
        var popped = false
        _uiState.update { currentState ->
            if (currentState.backStack.size > 1) {
                popped = true
                val newBackStack = currentState.backStack.dropLast(1)
                currentState.copy(
                    currentDestination = newBackStack.last(),
                    backStack = newBackStack
                )
            } else {
                currentState
            }
        }
        return popped
    }

    fun selectTopLevelDestination(destination: AppDestination) {
        _uiState.update { currentState ->
            currentState.copy(
                currentDestination = destination,
                backStack = listOf(destination)
            )
        }
    }
}
