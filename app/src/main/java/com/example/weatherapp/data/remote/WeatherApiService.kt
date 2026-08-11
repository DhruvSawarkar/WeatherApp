package com.example.weatherapp.data.remote

import com.example.weatherapp.data.remote.dto.CurrentWeatherResponse
import com.example.weatherapp.data.remote.dto.ForecastResponse
import com.example.weatherapp.data.remote.dto.GeocodingResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeatherMap REST endpoints. Free tier: current weather + 5day/3hour forecast
 * + geocoding (city name -> coordinates search).
 * Docs: https://openweathermap.org/api
 */
interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): CurrentWeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): ForecastResponse

    @GET("geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeocodingResult>

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }
}
