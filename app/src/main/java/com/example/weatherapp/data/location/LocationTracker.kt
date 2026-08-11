package com.example.weatherapp.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around FusedLocationProviderClient exposing:
 *  - a one-shot "last known / current" fetch for initial load
 *  - a live Flow of location updates for real-time tracking while the
 *    weather screen is open (used to silently refresh weather as the user moves).
 *
 * Caller is responsible for having already checked/requested runtime permissions.
 */
@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isGpsOrNetworkEnabled(): Boolean {
        val lm = context.getSystemService<LocationManager>() ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission") // caller checks permission before invoking
    suspend fun getCurrentLocation(): Location? {
        return try {
            fusedClient.lastLocation.await()
                ?: run {
                    val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                        .build()
                    fusedClient.getCurrentLocation(request, null).await()
                }
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(intervalMillis: Long = 60_000L): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, context.mainLooper)

        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }
}
