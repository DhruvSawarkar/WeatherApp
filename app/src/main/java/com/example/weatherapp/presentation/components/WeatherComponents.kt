package com.example.weatherapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.TempUnit
import com.example.weatherapp.util.toDayString
import com.example.weatherapp.util.toHourString
import com.example.weatherapp.util.toTempString
import com.example.weatherapp.util.weatherIconFor
import com.example.weatherapp.util.windDirectionLabel

@Composable
fun CurrentWeatherHero(current: CurrentWeather, unit: TempUnit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${current.locationName}, ${current.country}",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Icon(
            imageVector = weatherIconFor(current.iconCode),
            contentDescription = current.description,
            modifier = Modifier.size(96.dp)
        )
        Text(
            text = "${current.temperature.toTempString()}${unit.symbol.first()}",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = current.description.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Feels like ${current.feelsLike.toTempString()}${unit.symbol.first()} · H:${current.maxTemp.toTempString()} L:${current.minTemp.toTempString()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HourlyForecastRow(hourly: List<HourlyForecast>, tzOffset: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(hourly) { hour ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(hour.timestamp.toHourString(tzOffset), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(6.dp))
                    Icon(weatherIconFor(hour.iconCode), contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(hour.temperature.toTempString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun DailyForecastList(daily: List<DailyForecast>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 8.dp)) {
            daily.forEach { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(day.timestamp.toDayString(), modifier = Modifier.width(48.dp), style = MaterialTheme.typography.bodyLarge)
                    Icon(weatherIconFor(day.iconCode), contentDescription = day.condition, modifier = Modifier.size(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = day.minTemp.toTempString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(day.maxTemp.toTempString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsGrid(current: CurrentWeather, unit: TempUnit, modifier: Modifier = Modifier) {
    val items = listOf(
        "Humidity" to "${current.humidity}%",
        "Wind" to "${current.windSpeed} ${if (unit == TempUnit.CELSIUS) "m/s" else "mph"} ${windDirectionLabel(current.windDegree)}",
        "Pressure" to "${current.pressure} hPa",
        "Visibility" to "${current.visibilityMeters / 1000} km"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        items.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { (label, value) ->
                    Card(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.height(4.dp))
                            Text(value, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
