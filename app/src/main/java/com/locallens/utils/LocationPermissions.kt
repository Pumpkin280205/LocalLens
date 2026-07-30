package com.locallens.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object LocationPermissions {

    val coarse = Manifest.permission.ACCESS_COARSE_LOCATION
    val fine = Manifest.permission.ACCESS_FINE_LOCATION

    val all = arrayOf(coarse, fine)

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, coarse) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, fine) == PackageManager.PERMISSION_GRANTED
    }

    fun hasFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, fine) == PackageManager.PERMISSION_GRANTED
    }
}
