package com.example.weatherapp.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.location.LocationTracker
import com.example.weatherapp.data.prefs.UserPreferences
import com.example.weatherapp.domain.model.TempUnit
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns UI state for the weather screen. Two refresh paths:
 *  1. Explicit — initial load, pull-to-refresh, unit toggle, city search.
 *  2. Real-time — a live location Flow that re-fetches weather whenever the
 *     device moves meaningfully, so the app tracks the user's actual position
 *     rather than requiring a manual refresh.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var liveTrackingJob: Job? = null
    private var lastFetchedLocation: Location? = null

    init {
        viewModelScope.launch {
            val savedUnit = userPreferences.tempUnit.first()
            _uiState.value = _uiState.value.copy(unit = savedUnit)
        }
    }

    /** Call once, e.g. after location permission is granted, to start real-time tracking. */
    fun startLiveTracking() {
        liveTrackingJob?.cancel()
        liveTrackingJob = viewModelScope.launch {
            // Immediate first fetch from last-known/current location.
            locationTracker.getCurrentLocation()?.let { fetchWeatherFor(it) }

            // Then keep listening for movement and silently refresh.
            locationTracker.observeLocationUpdates().collectLatest { location ->
                if (_uiState.value.isLiveTrackingEnabled && hasMovedSignificantly(location)) {
                    fetchWeatherFor(location, isBackgroundRefresh = true)
                }
            }
        }
    }

    fun stopLiveTracking() {
        liveTrackingJob?.cancel()
    }

    fun toggleLiveTracking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isLiveTrackingEnabled = enabled)
        if (enabled) startLiveTracking() else stopLiveTracking()
    }

    fun refreshCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            val location = lastFetchedLocation ?: locationTracker.getCurrentLocation()
            if (location != null) {
                fetchWeatherFor(location)
            } else {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    isLoading = false,
                    errorMessage = "Couldn't determine your location. Check GPS is enabled."
                )
            }
        }
    }

    fun searchCity(query: String) {
        if (query.isBlank()) return
        stopLiveTracking()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, searchQuery = query)
            when (val result = repository.getWeatherByCity(query, _uiState.value.unit)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    weather = result.data,
                    lastUpdatedMillis = System.currentTimeMillis(),
                    errorMessage = null
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = result.message
                )
                Result.Loading -> Unit
            }
        }
    }

    fun setUnit(unit: TempUnit) {
        if (unit == _uiState.value.unit) return
        _uiState.value = _uiState.value.copy(unit = unit)
        viewModelScope.launch { userPreferences.setTempUnit(unit) }
        // Re-fetch with the new unit so temps come pre-converted from the API.
        lastFetchedLocation?.let { fetchWeatherFor(it) }
            ?: uiState.value.searchQuery.takeIf { it.isNotBlank() }?.let { searchCity(it) }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun fetchWeatherFor(location: Location, isBackgroundRefresh: Boolean = false) {
        lastFetchedLocation = location
        viewModelScope.launch {
            if (!isBackgroundRefresh) {
                _uiState.value = _uiState.value.copy(isLoading = _uiState.value.weather == null, isRefreshing = true)
            }
            when (val result = repository.getWeather(location.latitude, location.longitude, _uiState.value.unit)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    weather = result.data,
                    lastUpdatedMillis = System.currentTimeMillis(),
                    errorMessage = null
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    // Don't clobber the UI with an error toast for a silent background refresh.
                    errorMessage = if (isBackgroundRefresh) _uiState.value.errorMessage else result.message
                )
                Result.Loading -> Unit
            }
        }
    }

    private fun hasMovedSignificantly(newLocation: Location): Boolean {
        val last = lastFetchedLocation ?: return true
        // Re-fetch only past ~1.5km of movement to avoid hammering the API.
        return last.distanceTo(newLocation) > 1500f
    }

    override fun onCleared() {
        super.onCleared()
        liveTrackingJob?.cancel()
    }
}
