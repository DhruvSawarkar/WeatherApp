package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.model.TempUnit
import com.example.weatherapp.domain.model.WeatherBundle
import com.example.weatherapp.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing contract. The ViewModel depends on this abstraction only,
 * never on Retrofit/DTO types — makes the data source swappable and testable.
 */
interface WeatherRepository {

    /** One-shot fetch by coordinates. */
    suspend fun getWeather(
        lat: Double,
        lon: Double,
        unit: TempUnit
    ): Result<WeatherBundle>

    /** One-shot fetch by city name search. */
    suspend fun getWeatherByCity(
        cityName: String,
        unit: TempUnit
    ): Result<WeatherBundle>

    /** Cached last-known bundle, for instant UI while a fresh fetch is in flight. */
    fun observeCachedWeather(): Flow<WeatherBundle?>
}
