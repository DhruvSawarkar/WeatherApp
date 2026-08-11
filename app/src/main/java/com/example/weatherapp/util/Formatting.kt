package com.example.weatherapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun Double.toTempString(): String = "${roundToInt()}°"

fun Long.toHourString(tzOffsetSeconds: Int = 0): String {
    val sdf = SimpleDateFormat("h a", Locale.getDefault())
    return sdf.format(Date((this + tzOffsetSeconds) * 1000))
}

fun Long.toDayString(): String {
    val sdf = SimpleDateFormat("EEE", Locale.getDefault())
    return sdf.format(Date(this * 1000))
}

fun Long.toFullDateString(tzOffsetSeconds: Int = 0): String {
    val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    return sdf.format(Date((this + tzOffsetSeconds) * 1000))
}

fun Long.toClockString(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(this * 1000))
}

fun windDirectionLabel(degree: Int): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = (((degree % 360) / 45.0).roundToInt()) % 8
    return directions[index]
}
