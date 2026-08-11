package com.example.weatherapp.presentation.viewmodel

import com.example.weatherapp.domain.model.TempUnit
import com.example.weatherapp.domain.model.WeatherBundle

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val weather: WeatherBundle? = null,
    val errorMessage: String? = null,
    val unit: TempUnit = TempUnit.CELSIUS,
    val lastUpdatedMillis: Long? = null,
    val isLiveTrackingEnabled: Boolean = true,
    val searchQuery: String = ""
)
