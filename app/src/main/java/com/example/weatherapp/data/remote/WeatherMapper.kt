package com.example.weatherapp.data.remote

import com.example.weatherapp.data.remote.dto.CurrentWeatherResponse
import com.example.weatherapp.data.remote.dto.ForecastResponse
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.WeatherBundle
import java.util.Calendar
import java.util.TimeZone

/** Maps raw OpenWeatherMap DTOs into clean domain models used by the UI. */
fun CurrentWeatherResponse.toDomain(): CurrentWeather {
    val condition = weather.firstOrNull()
    return CurrentWeather(
        locationName = name,
        country = sys.country,
        temperature = main.temp,
        feelsLike = main.feelsLike,
        minTemp = main.tempMin,
        maxTemp = main.tempMax,
        humidity = main.humidity,
        pressure = main.pressure,
        windSpeed = wind.speed,
        windDegree = wind.deg,
        condition = condition?.main.orEmpty(),
        description = condition?.description.orEmpty(),
        iconCode = condition?.icon.orEmpty(),
        sunrise = sys.sunrise,
        sunset = sys.sunset,
        timezoneOffsetSeconds = timezone,
        observedAt = dt,
        visibilityMeters = visibility
    )
}

/**
 * The free /forecast endpoint returns 3-hour steps for 5 days. We derive:
 *  - hourly: the next 8 raw steps (~24h)
 *  - daily: one aggregated min/max per calendar day, using the midday-ish sample
 *    for the representative icon/condition.
 */
fun ForecastResponse.toDomainBundle(current: CurrentWeather): WeatherBundle {
    val tz = TimeZone.getTimeZone("UTC").also { it.rawOffset = city.timezone * 1000 }

    val hourly = list.take(8).map {
        HourlyForecast(
            timestamp = it.dt,
            temperature = it.main.temp,
            iconCode = it.weather.firstOrNull()?.icon.orEmpty(),
            precipitationProbability = it.pop
        )
    }

    val byDay = list.groupBy { item ->
        val cal = Calendar.getInstance(tz)
        cal.timeInMillis = item.dt * 1000
        cal.get(Calendar.DAY_OF_YEAR)
    }

    val daily = byDay.values.mapNotNull { dayItems ->
        if (dayItems.isEmpty()) return@mapNotNull null
        val minT = dayItems.minOf { it.main.tempMin }
        val maxT = dayItems.maxOf { it.main.tempMax }
        val maxPop = dayItems.maxOf { it.pop }
        // Prefer the sample closest to midday local time as the representative icon.
        val representative = dayItems.minByOrNull { item ->
            val cal = Calendar.getInstance(tz)
            cal.timeInMillis = item.dt * 1000
            kotlin.math.abs(cal.get(Calendar.HOUR_OF_DAY) - 13)
        } ?: dayItems.first()

        DailyForecast(
            timestamp = representative.dt,
            minTemp = minT,
            maxTemp = maxT,
            iconCode = representative.weather.firstOrNull()?.icon.orEmpty(),
            condition = representative.weather.firstOrNull()?.main.orEmpty(),
            precipitationProbability = maxPop
        )
    }.sortedBy { it.timestamp }

    return WeatherBundle(current = current, hourly = hourly, daily = daily)
}
