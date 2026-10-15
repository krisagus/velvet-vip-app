package com.example.my1eraapplication.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CounterViewModelTest {

    private lateinit var viewModel: CounterViewModel

    @Before
    fun setUp() {
        viewModel = CounterViewModel()
    }

    @Test
    fun counterViewModel_initialState_isZero() {
        val state = viewModel.uiState.value
        assertEquals(0, state.count)
        assertEquals(1, state.step)
        assertEquals(listOf(0), state.history)
    }

    @Test
    fun increment_increasesCountByStep() {
        viewModel.increment()
        assertEquals(1, viewModel.uiState.value.count)
        assertEquals(listOf(0, 1), viewModel.uiState.value.history)
    }

    @Test
    fun decrement_decreasesCountByStep() {
        viewModel.decrement()
        assertEquals(-1, viewModel.uiState.value.count)
    }

    @Test
    fun setStep_changesIncrementStep() {
        viewModel.setStep(5)
        assertEquals(5, viewModel.uiState.value.step)
        viewModel.increment()
        assertEquals(5, viewModel.uiState.value.count)
    }

    @Test
    fun reset_resetsCountToZero() {
        viewModel.increment()
        viewModel.increment()
        viewModel.reset()
        assertEquals(0, viewModel.uiState.value.count)
    }
}
