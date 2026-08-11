package com.example.weatherapp.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps OpenWeatherMap icon codes (e.g. "01d", "10n") to Material icons. */
fun weatherIconFor(code: String): ImageVector = when (code.take(2)) {
    "01" -> Icons.Filled.WbSunny
    "02", "03" -> Icons.Filled.CloudQueue
    "04" -> Icons.Filled.Cloud
    "09" -> Icons.Filled.WaterDrop
    "10" -> Icons.Filled.Grain
    "11" -> Icons.Filled.Thunderstorm
    "13" -> Icons.Filled.AcUnit
    "50" -> Icons.Filled.CloudQueue
    else -> Icons.Filled.WbSunny
}

fun isNightIcon(code: String): Boolean = code.endsWith("n")
