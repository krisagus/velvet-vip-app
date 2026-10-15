package com.example.my1eraapplication.model

import kotlinx.serialization.Serializable

sealed interface AppDestination {
    @Serializable
    data object Home : AppDestination

    @Serializable
    data object Profile : AppDestination

    @Serializable
    data object Counter : AppDestination

    @Serializable
    data object Settings : AppDestination
}

data class NavigationUiState(
    val currentDestination: AppDestination = AppDestination.Home,
    val backStack: List<AppDestination> = listOf(AppDestination.Home)
) {
    val canNavigateBack: Boolean get() = backStack.size > 1
}
