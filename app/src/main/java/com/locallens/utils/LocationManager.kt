package com.locallens.utils

import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "LocationManager"
private const val FRESH_LOCATION_TIMEOUT_MS = 15_000L
private const val CACHED_LOCATION_MAX_AGE_MS = 5 * 60 * 1000L

suspend fun fetchFreshLocation(
    fusedClient: FusedLocationProviderClient
): LocationResult? {
    val highAccResult = requestFreshLocation(
        fusedClient,
        Priority.PRIORITY_HIGH_ACCURACY,
        FRESH_LOCATION_TIMEOUT_MS
    )
    if (highAccResult != null && highAccResult.firstLocation() != null) {
        Log.d(TAG, "Got fresh high-accuracy location")
        return highAccResult
    }

    val balancedResult = requestFreshLocation(
        fusedClient,
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        10_000L
    )
    if (balancedResult != null && balancedResult.firstLocation() != null) {
        Log.d(TAG, "Got fresh balanced-priority location")
        return balancedResult
    }

    val cachedResult = getLastLocationCached(fusedClient)
    if (cachedResult != null && cachedResult.firstLocation() != null) {
        Log.d(TAG, "Using cached last location")
        return cachedResult
    }

    Log.w(TAG, "All location attempts failed")
    return null
}

private suspend fun requestFreshLocation(
    fusedClient: FusedLocationProviderClient,
    priority: Int,
    timeoutMs: Long
): LocationResult? {
    return withTimeoutOrNull(timeoutMs) {
        val deferred = CompletableDeferred<LocationResult?>()

        val locationRequest = LocationRequest.Builder(priority, 0L)
            .setMaxUpdateAgeMillis(0L) // Fresh only
            .setMinUpdateIntervalMillis(0L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                deferred.complete(result)
                try {
                    fusedClient.removeLocationUpdates(this)
                } catch (_: Exception) { /* ignore */ }
            }
        }

        try {
            fusedClient.requestLocationUpdates(locationRequest, callback, null)
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission not granted for priority $priority", e)
            deferred.complete(null)
            return@withTimeoutOrNull null
        } catch (e: Exception) {
            Log.w(TAG, "Location request failed for priority $priority", e)
            deferred.complete(null)
            return@withTimeoutOrNull null
        }

        deferred.await()
    }
}

private suspend fun getLastLocationCached(
    fusedClient: FusedLocationProviderClient
): LocationResult? {
    return withContext(Dispatchers.IO) {
        try {
            val task = fusedClient.lastLocation
            Tasks.await(task)
            val location = task.result
            if (location != null && isLocationFresh(location)) {
                LocationResult.create(listOf(location))
            } else {
                null
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "No permission for getLastLocation", e)
            null
        } catch (e: Exception) {
            Log.w(TAG, "getLastLocation failed", e)
            null
        }
    }
}

private fun isLocationFresh(location: Location): Boolean {
    val ageMs = System.currentTimeMillis() - location.time
    return ageMs <= CACHED_LOCATION_MAX_AGE_MS
}

fun LocationResult?.firstLocation(): Location? =
    this?.lastLocation ?: this?.locations?.firstOrNull()