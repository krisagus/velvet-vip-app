package com.example.my1eraapplication.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class ThemeUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isDynamicColorEnabled: Boolean = true
)
