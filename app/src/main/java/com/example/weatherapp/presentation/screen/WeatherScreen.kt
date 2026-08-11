package com.example.weatherapp.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.util.toClockString
import com.example.weatherapp.R
import com.example.weatherapp.domain.model.TempUnit
import com.example.weatherapp.presentation.components.CurrentWeatherHero
import com.example.weatherapp.presentation.components.DailyForecastList
import com.example.weatherapp.presentation.components.HourlyForecastRow
import com.example.weatherapp.presentation.components.LocationPermissionGate
import com.example.weatherapp.presentation.components.WeatherDetailsGrid
import com.example.weatherapp.presentation.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onOpenAppSettings: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (searchExpanded) {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("Search city…") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    if (searchExpanded) {
                        IconButton(onClick = {
                            if (searchText.isNotBlank()) viewModel.searchCity(searchText)
                            searchExpanded = false
                        }) { Icon(Icons.Filled.Search, contentDescription = "Search") }
                    } else {
                        IconButton(onClick = { searchExpanded = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search city")
                        }
                        IconButton(onClick = { viewModel.toggleLiveTracking(!uiState.isLiveTrackingEnabled) }) {
                            Icon(
                                imageVector = if (uiState.isLiveTrackingEnabled) Icons.Filled.GpsFixed else Icons.Filled.GpsOff,
                                contentDescription = "Toggle live location tracking"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            LocationPermissionGate(onOpenSettings = onOpenAppSettings) {
                LaunchedEffect(Unit) { viewModel.startLiveTracking() }

                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refreshCurrentLocation() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        uiState.isLoading && uiState.weather == null -> LoadingState()
                        uiState.weather != null -> WeatherContent(
                            uiState = uiState,
                            onUnitSelected = viewModel::setUnit
                        )
                        else -> EmptyState()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Finding your local weather…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No weather data yet. Pull to refresh.", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun WeatherContent(
    uiState: com.example.weatherapp.presentation.viewmodel.WeatherUiState,
    onUnitSelected: (TempUnit) -> Unit
) {
    val bundle = uiState.weather ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TempUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = uiState.unit == unit,
                        onClick = { onUnitSelected(unit) },
                        label = { Text(unit.symbol) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
        item { CurrentWeatherHero(bundle.current, uiState.unit) }
        item {
            Text("Hourly Forecast", style = MaterialTheme.typography.titleLarge)
        }
        item { HourlyForecastRow(bundle.hourly, bundle.current.timezoneOffsetSeconds) }
        item {
            Text("7-Day Forecast", style = MaterialTheme.typography.titleLarge)
        }
        item { DailyForecastList(bundle.daily) }
        item {
            Text("Details", style = MaterialTheme.typography.titleLarge)
        }
        item { WeatherDetailsGrid(bundle.current, uiState.unit) }
        item {
            uiState.lastUpdatedMillis?.let {
                Text(
                    text = "Updated ${(it / 1000).toClockString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
