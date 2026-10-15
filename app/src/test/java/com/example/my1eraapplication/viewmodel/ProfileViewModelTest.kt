package com.example.my1eraapplication.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        viewModel = ProfileViewModel()
    }

    @Test
    fun profileViewModel_initialState_isDefault() {
        val state = viewModel.uiState.value
        assertEquals("Jane Doe", state.profile.name)
        assertEquals("jane.doe@example.com", state.profile.email)
        assertFalse(state.isEditing)
        assertFalse(state.isLoading)
    }

    @Test
    fun updateName_changesProfileName() {
        viewModel.updateName("John Smith")
        assertEquals("John Smith", viewModel.uiState.value.profile.name)
    }

    @Test
    fun updateEmail_changesProfileEmail() {
        viewModel.updateEmail("john.smith@example.com")
        assertEquals("john.smith@example.com", viewModel.uiState.value.profile.email)
    }

    @Test
    fun updateBio_changesProfileBio() {
        viewModel.updateBio("Kotlin Developer")
        assertEquals("Kotlin Developer", viewModel.uiState.value.profile.bio)
    }

    @Test
    fun setEditing_updatesEditingState() {
        viewModel.setEditing(true)
        assertTrue(viewModel.uiState.value.isEditing)
        viewModel.setEditing(false)
        assertFalse(viewModel.uiState.value.isEditing)
    }

    @Test
    fun resetProfile_resetsToDefaultState() {
        viewModel.updateName("Custom Name")
        viewModel.setEditing(true)
        viewModel.resetProfile()

        val state = viewModel.uiState.value
        assertEquals("Jane Doe", state.profile.name)
        assertFalse(state.isEditing)
    }
}
