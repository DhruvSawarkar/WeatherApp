package com.example.weatherapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs mirror the OpenWeatherMap "Current Weather" + "One Call 3.0" JSON shapes.
 * Kept separate from domain models so a future provider swap only touches
 * this file and the mapper, never the UI.
 */

data class CurrentWeatherResponse(
    @SerializedName("coord") val coord: CoordDto,
    @SerializedName("weather") val weather: List<WeatherConditionDto>,
    @SerializedName("main") val main: MainDto,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("sys") val sys: SysDto,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("name") val name: String,
    @SerializedName("dt") val dt: Long
)

data class CoordDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

data class WeatherConditionDto(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class MainDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
)

data class WindDto(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int
)

data class SysDto(
    @SerializedName("country") val country: String,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

// --- 5-day / 3-hour forecast endpoint (free tier substitute for One Call) ---

data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItemDto>,
    @SerializedName("city") val city: ForecastCityDto
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainDto,
    @SerializedName("weather") val weather: List<WeatherConditionDto>,
    @SerializedName("pop") val pop: Double // probability of precipitation 0.0-1.0
)

data class ForecastCityDto(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("timezone") val timezone: Int
)

data class GeocodingResult(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String
)
