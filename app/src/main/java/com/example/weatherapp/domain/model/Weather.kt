package com.example.weatherapp.domain.model

/**
 * Clean domain model exposed to the UI layer — decoupled from network DTOs
 * so the API/provider can change without touching presentation code.
 */
data class CurrentWeather(
    val locationName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDegree: Int,
    val condition: String,
    val description: String,
    val iconCode: String,
    val sunrise: Long,
    val sunset: Long,
    val timezoneOffsetSeconds: Int,
    val observedAt: Long,
    val visibilityMeters: Int
)

data class HourlyForecast(
    val timestamp: Long,
    val temperature: Double,
    val iconCode: String,
    val precipitationProbability: Double
)

data class DailyForecast(
    val timestamp: Long,
    val minTemp: Double,
    val maxTemp: Double,
    val iconCode: String,
    val condition: String,
    val precipitationProbability: Double
)

data class WeatherBundle(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)

enum class TempUnit(val apiParam: String, val symbol: String) {
    CELSIUS("metric", "°C"),
    FAHRENHEIT("imperial", "°F")
}
