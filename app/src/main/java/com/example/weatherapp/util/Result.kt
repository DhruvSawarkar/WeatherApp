package com.example.weatherapp.util

/**
 * Lightweight sealed wrapper so the UI layer can render loading/error/success
 * states without leaking exceptions or Retrofit response types upward.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
