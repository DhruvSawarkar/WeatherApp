package com.example.weatherapp.presentation.components

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Requests fine/coarse location permission and gates content behind it.
 * - Granted -> onGranted() content (starts real-time tracking).
 * - Denied -> rationale + retry button.
 * - Permanently denied -> prompt to open Settings.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionGate(

    onOpenSettings: () -> Unit,
    onGranted: @Composable () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    if (permissionState.allPermissionsGranted) {
        onGranted()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.padding(bottom = 16.dp))
            Text(
                text = "Weather needs your location",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Grant location access to get real-time weather for wherever you are.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            val allPermanentlyDenied = permissionState.permissions.all {
                !it.status.isGranted && !it.status.shouldShowRationale
            }
            Button(onClick = {
                if (allPermanentlyDenied) onOpenSettings()
                else permissionState.launchMultiplePermissionRequest()
            }) {
                Text(if (allPermanentlyDenied) "Open Settings" else "Grant Permission")
            }
        }
    }
}
