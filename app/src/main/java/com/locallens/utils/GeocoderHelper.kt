package com.locallens.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.locallens.data.model.Country
import java.io.IOException
import java.util.Locale

private const val TAG = "GeocoderHelper"


suspend fun reverseGeocodeCountry(
    context: Context,
    latitude: Double,
    longitude: Double
): Country? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses.isNullOrEmpty()) {
            Log.w(TAG, "No addresses found for [$latitude, $longitude]")
            null
        } else {
            val address = addresses[0]
            val countryName = address.locality
                ?: address.subAdminArea
                ?: address.adminArea
                ?: address.countryName
                ?: "Unknown"
            val countryCode = address.countryCode
                ?: "XX"

            Country(name = countryName, code = countryCode)
        }
    } catch (e: IOException) {
        Log.w(TAG, "Geocoder service unavailable", e)
        null
    } catch (e: Exception) {
        Log.w(TAG, "Geocoding failed for [$latitude, $longitude]", e)
        null
    }
}
