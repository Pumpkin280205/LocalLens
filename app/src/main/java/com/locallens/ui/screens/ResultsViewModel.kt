package com.locallens.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.locallens.data.model.Article
import com.locallens.data.repository.NewsRepository
import com.locallens.api.NetworkResult
import com.locallens.ui.state.ArticleUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NewsRepository.getInstance()

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    private var currentCountryCode: String = ""
    private var fetchJob: Job? = null

    fun loadNews(countryCode: String) {
        fetchJob?.cancel()
        currentCountryCode = countryCode
        _uiState.update {
            it.copy(
                articles = emptyList(),
                isLoading = true,
                isLoadingMore = false,
                error = null,
                hasMore = true,
                totalResults = 0,
                currentPage = 0
            )
        }
        fetchPage(page = 0)
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore) return
        val nextPage = _uiState.value.currentPage + 1
        _uiState.update { it.copy(isLoadingMore = true, error = null) }
        fetchPage(page = nextPage)
    }

    private fun fetchPage(page: Int) {
        fetchJob = viewModelScope.launch {
            val result = repository.getNews(
                countryCode = currentCountryCode,
                category = "general",
                page = page,
                pageSize = 10
            )
            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val isFirstPage = page == 0
                    val allArticles = if (isFirstPage) data.articles else _uiState.value.articles + data.articles

                    _uiState.update {
                        it.copy(
                            articles = allArticles,
                            isLoading = false,
                            isLoadingMore = false,
                            totalResults = data.totalResults,
                            currentPage = data.currentPage,
                            pageSize = data.pageSize,
                            hasMore = allArticles.size < data.totalResults,
                            error = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}