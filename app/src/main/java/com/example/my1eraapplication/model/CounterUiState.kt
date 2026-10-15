package com.example.my1eraapplication.model

data class CounterUiState(
    val count: Int = 0,
    val step: Int = 1,
    val history: List<Int> = listOf(0)
)
