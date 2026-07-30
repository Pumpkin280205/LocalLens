package com.locallens.ui.state

import com.locallens.data.model.Article

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>()
    data class PermissionDenied(val message: String) : UiState<Nothing>()
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val locationIsLoading: Boolean = false,
    val locationError: String? = null,
    val selectedCountryName: String = "Select a country",
    val selectedCountryCode: String = "",
    val selectedCategory: String = "general",
    val hasLocationPermission: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationCountry: String? = null,
    val canLoadNews: Boolean = false
)

data class ArticleUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val totalResults: Int = 0,
    val currentPage: Int = 0,
    val pageSize: Int = 10
)
