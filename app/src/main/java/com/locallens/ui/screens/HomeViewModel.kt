package com.locallens.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.locallens.data.model.Country
import com.locallens.data.repository.NewsRepository
import com.locallens.ui.state.HomeUiState
import com.locallens.utils.CountriesRepository
import com.locallens.utils.LocationPermissions
import com.locallens.utils.fetchFreshLocation
import com.locallens.utils.firstLocation
import com.locallens.utils.reverseGeocodeCountry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NewsRepository.getInstance()
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun checkPermissionStatus() {
        val context = getApplication<Application>()
        _uiState.update { it.copy(
            hasLocationPermission = LocationPermissions.hasLocationPermission(context)
        )}
    }

    fun onCountrySelected(country: Country) {
        _uiState.update {
            it.copy(
                selectedCountryName = country.name,
                selectedCountryCode = country.code,
                canLoadNews = country.code.isNotBlank(),
                locationError = null
            )
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onUseMyLocation() {
        val context = getApplication<Application>()

        // Check if we already have permission
        if (!LocationPermissions.hasLocationPermission(context)) {
            _uiState.update {
                it.copy(
                    locationIsLoading = false,
                    locationError = "Location permission required. Please grant permission in settings.",
                    hasLocationPermission = false
                )
            }
            return
        }

        // We have permission - fetch location
        _uiState.update { it.copy(locationIsLoading = true, locationError = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val locationResult = fetchFreshLocation(fusedClient)
                val location = locationResult.firstLocation()

                if (location != null) {
                    val country = reverseGeocodeCountry(
                        context, location.latitude, location.longitude
                    )

                    if (country != null) {
                        _uiState.update {
                            it.copy(
                                locationIsLoading = false,
                                selectedCountryName = country.name,
                                selectedCountryCode = country.code,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                locationCountry = country.name,
                                canLoadNews = true,
                                hasLocationPermission = true,
                                locationError = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                locationIsLoading = false,
                                locationError = "Could not determine country from location. Try selecting manually.",
                                hasLocationPermission = true
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            locationIsLoading = false,
                            locationError = "Unable to get location. Try again or select a country manually.",
                            hasLocationPermission = true
                        )
                    }
                }
            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(
                        locationIsLoading = false,
                        locationError = "Location permission revoked. Please re-grant in settings.",
                        hasLocationPermission = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        locationIsLoading = false,
                        locationError = "Location error: ${e.message}",
                        hasLocationPermission = true
                    )
                }
            }
        }
    }

    fun onLocationResolved(country: Country, latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                locationIsLoading = false,
                selectedCountryName = country.name,
                selectedCountryCode = country.code,
                latitude = latitude,
                longitude = longitude,
                locationCountry = country.name,
                canLoadNews = country.code.isNotBlank(),
                hasLocationPermission = true,
                locationError = null
            )
        }
    }

    fun onLocationPermissionDenied(message: String = "Location permission denied. Use the country picker instead.") {
        _uiState.update {
            it.copy(
                locationIsLoading = false,
                locationError = message,
                hasLocationPermission = false
            )
        }
    }

    fun onLocationFailed(message: String) {
        _uiState.update {
            it.copy(
                locationIsLoading = false,
                locationError = message,
                hasLocationPermission = false
            )
        }
    }
}