package com.example.my1eraapplication.model

data class UserProfile(
    val name: String = "Jane Doe",
    val email: String = "jane.doe@example.com",
    val bio: String = "Android enthusiast & Jetpack Compose developer",
    val avatarUrl: String = "",
    val phoneNumber: String = "+1 555-0199"
)

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
