package com.locallens.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.locallens.data.model.Country
import com.locallens.ui.state.HomeUiState
import com.locallens.utils.CountriesRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState = HomeUiState(),
    onUseMyLocation: () -> Unit = {},
    onCountrySelected: (Country) -> Unit = {},
    onLoadNews: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "LocalLens",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Discover news by your location",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Location card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.locationIsLoading) "Detecting location..."
                            else if (uiState.selectedCountryCode.isNotEmpty()) "${uiState.selectedCountryName} (${uiState.selectedCountryCode})"
                            else "No country selected",
                            fontWeight = FontWeight.Medium
                        )
                        if (uiState.latitude != null && uiState.longitude != null) {
                            Text(
                                text = "${uiState.latitude}, ${uiState.longitude}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (uiState.locationError != null) {
                            Text(
                                text = uiState.locationError,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onUseMyLocation,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.locationIsLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use my location")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Country picker
        Text(
            text = "Or select a country",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        var countryPickerExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = countryPickerExpanded,
            onExpandedChange = { countryPickerExpanded = !countryPickerExpanded }
        ) {
            OutlinedTextField(
                value = uiState.selectedCountryName,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Choose a country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryPickerExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = countryPickerExpanded,
                onDismissRequest = { countryPickerExpanded = false }
            ) {
                CountriesRepository.all.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country.name) },
                        onClick = {
                            onCountrySelected(country)
                            countryPickerExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Load news button
        Button(
            onClick = onLoadNews,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedCountryCode.isNotEmpty() && uiState.canLoadNews
        ) {
            Text("Load News")
        }
    }
}