package com.example.my1eraapplication.viewmodel

import com.example.my1eraapplication.model.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NavigationViewModelTest {

    private lateinit var viewModel: NavigationViewModel

    @Before
    fun setUp() {
        viewModel = NavigationViewModel()
    }

    @Test
    fun navigationViewModel_initialState_isHome() {
        val state = viewModel.uiState.value
        assertEquals(AppDestination.Home, state.currentDestination)
        assertEquals(listOf(AppDestination.Home), state.backStack)
        assertFalse(state.canNavigateBack)
    }

    @Test
    fun navigateTo_addsToBackStackAndUpdatesDestination() {
        viewModel.navigateTo(AppDestination.Profile)
        val state = viewModel.uiState.value
        assertEquals(AppDestination.Profile, state.currentDestination)
        assertEquals(listOf(AppDestination.Home, AppDestination.Profile), state.backStack)
        assertTrue(state.canNavigateBack)
    }

    @Test
    fun popBackStack_popsTopDestinationAndReturnsTrue() {
        viewModel.navigateTo(AppDestination.Profile)
        val popped = viewModel.popBackStack()
        assertTrue(popped)
        val state = viewModel.uiState.value
        assertEquals(AppDestination.Home, state.currentDestination)
        assertFalse(state.canNavigateBack)
    }

    @Test
    fun popBackStack_atRoot_returnsFalseAndDoesNotPop() {
        val popped = viewModel.popBackStack()
        assertFalse(popped)
        assertEquals(AppDestination.Home, viewModel.uiState.value.currentDestination)
    }

    @Test
    fun selectTopLevelDestination_resetsBackStackToSingleDestination() {
        viewModel.navigateTo(AppDestination.Profile)
        viewModel.navigateTo(AppDestination.Counter)
        viewModel.selectTopLevelDestination(AppDestination.Settings)

        val state = viewModel.uiState.value
        assertEquals(AppDestination.Settings, state.currentDestination)
        assertEquals(listOf(AppDestination.Settings), state.backStack)
        assertFalse(state.canNavigateBack)
    }
}
