package com.example.my1eraapplication.viewmodel

import com.example.my1eraapplication.model.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ThemeViewModelTest {

    private lateinit var viewModel: ThemeViewModel

    @Before
    fun setUp() {
        viewModel = ThemeViewModel()
    }

    @Test
    fun themeViewModel_initialState_isSystemWithDynamicColor() {
        val state = viewModel.uiState.value
        assertEquals(AppThemeMode.SYSTEM, state.themeMode)
        assertTrue(state.isDynamicColorEnabled)
    }

    @Test
    fun setThemeMode_updatesThemeMode() {
        viewModel.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun setDynamicColorEnabled_updatesDynamicColorFlag() {
        viewModel.setDynamicColorEnabled(false)
        assertFalse(viewModel.uiState.value.isDynamicColorEnabled)
    }

    @Test
    fun toggleThemeMode_cyclesThroughThemeModes() {
        assertEquals(AppThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
        viewModel.toggleThemeMode()
        assertEquals(AppThemeMode.LIGHT, viewModel.uiState.value.themeMode)
        viewModel.toggleThemeMode()
        assertEquals(AppThemeMode.DARK, viewModel.uiState.value.themeMode)
        viewModel.toggleThemeMode()
        assertEquals(AppThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }
}
