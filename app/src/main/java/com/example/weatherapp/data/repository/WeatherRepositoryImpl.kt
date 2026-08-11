package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.remote.WeatherApiService
import com.example.weatherapp.data.remote.toDomain
import com.example.weatherapp.data.remote.toDomainBundle
import com.example.weatherapp.domain.model.TempUnit
import com.example.weatherapp.domain.model.WeatherBundle
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApiService
) : WeatherRepository {

    // Simple in-memory cache of the last successful fetch, so the UI can show
    // something instantly on next launch/refresh while a new network call runs.
    private val cache: MutableStateFlow<WeatherBundle?> = MutableStateFlow(null)

    override suspend fun getWeather(lat: Double, lon: Double, unit: TempUnit): Result<WeatherBundle> =
        safeCall {
            val currentResponse = api.getCurrentWeather(lat, lon, unit.apiParam, BuildConfig.WEATHER_API_KEY)
            val forecastResponse = api.getForecast(lat, lon, unit.apiParam, BuildConfig.WEATHER_API_KEY)
            val current = currentResponse.toDomain()
            val bundle = forecastResponse.toDomainBundle(current)
            cache.value = bundle
            bundle
        }

    override suspend fun getWeatherByCity(cityName: String, unit: TempUnit): Result<WeatherBundle> =
        safeCall {
            val geocode = api.searchCity(cityName = cityName, apiKey = BuildConfig.WEATHER_API_KEY)
            val match = geocode.firstOrNull()
                ?: throw IOException("No results found for \"$cityName\"")
            val currentResponse = api.getCurrentWeather(match.lat, match.lon, unit.apiParam, BuildConfig.WEATHER_API_KEY)
            val forecastResponse = api.getForecast(match.lat, match.lon, unit.apiParam, BuildConfig.WEATHER_API_KEY)
            val current = currentResponse.toDomain()
            val bundle = forecastResponse.toDomainBundle(current)
            cache.value = bundle
            bundle
        }

    override fun observeCachedWeather(): StateFlow<WeatherBundle?> = cache

    private inline fun <T> safeCall(block: () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                401 -> "Invalid or missing API key. Add yours in local.properties."
                404 -> "Location not found."
                429 -> "Rate limit exceeded. Try again shortly."
                else -> "Server error (${e.code()}). Please try again."
            }
            Result.Error(msg, e)
        } catch (e: IOException) {
            Result.Error(e.message ?: "Network error. Check your connection.", e)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unexpected error.", e)
        }
    }
}
