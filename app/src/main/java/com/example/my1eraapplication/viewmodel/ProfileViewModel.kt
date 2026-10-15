package com.example.my1eraapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.my1eraapplication.model.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(profile = currentState.profile.copy(name = name))
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(profile = currentState.profile.copy(email = email))
        }
    }

    fun updateBio(bio: String) {
        _uiState.update { currentState ->
            currentState.copy(profile = currentState.profile.copy(bio = bio))
        }
    }

    fun updateAvatarUrl(url: String) {
        _uiState.update { currentState ->
            currentState.copy(profile = currentState.profile.copy(avatarUrl = url))
        }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.update { currentState ->
            currentState.copy(profile = currentState.profile.copy(phoneNumber = phone))
        }
    }

    fun setEditing(isEditing: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isEditing = isEditing)
        }
    }

    fun resetProfile() {
        _uiState.update { ProfileUiState() }
    }
}
