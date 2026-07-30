package com.locallens.data.repository

import com.locallens.BuildConfig
import com.locallens.api.NetworkResult
import com.locallens.api.RetrofitClient
import com.locallens.api.WorldNewsApiService
import com.locallens.data.model.Article
import com.locallens.data.model.NewsResponse
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewsRepository private constructor() {

    private val apiKey: String = BuildConfig.API_KEY
    init {
        Log.d("NewsRepository", "BuildConfig.API_KEY = '$apiKey' (length=${apiKey.length}, isBlank=${apiKey.isBlank()})")
    }
    private val service: WorldNewsApiService by lazy {
        RetrofitClient.create(WorldNewsApiService::class.java)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun getNews(
        countryCode: String,
        category: String? = null,
        page: Int = 0,
        pageSize: Int = 10
    ): NetworkResult<NewsResult> {
        // Guard: missing API key
        if (apiKey.isBlank()) {
            return NetworkResult.Error("API key is missing. Add it to local.properties.")
        }

        return try {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -2) // fetch from 2 days ago
            val earliestPublishDate = dateFormat.format(cal.time)

            Log.d("NewsRepository", "Fetching news: country=$countryCode, category=$category, date=$earliestPublishDate, page=$page, pageSize=$pageSize")

            val response = service.searchNews(
                sourceCountries = countryCode,
                language = "en",
                earliestPublishDate = earliestPublishDate,
                number = pageSize,
                offset = page * pageSize,
                apiKey = apiKey,
                category = category
            )

            Log.d("NewsRepository", "Request sent with category parameter: $category")

            Log.d("NewsRepository", "Raw response: totalResults=${response.totalResults}, articlesSize=${response.articles?.size}, articles=${response.articles?.take(2)}")

            val articles = response.articles ?: emptyList()
            val totalResults = response.totalResults ?: articles.size

            NetworkResult.Success(
                NewsResult(
                    articles = articles.map { article ->
                        article.copy(
                            title = article.title ?: "No headline",
                            summary = article.summary,
                            authors = article.authors,
                            category = article.category,
                            imageUrl = article.imageUrl,
                            url = article.url,
                            publicationDate = article.publicationDate,
                            sourceCountry = article.sourceCountry,
                            sentiment = article.sentiment
                        )
                    },
                    totalResults = totalResults,
                    currentPage = page,
                    pageSize = pageSize
                )
            )
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("401", ignoreCase = true) == true -> {
                    android.util.Log.e("NewsRepository", "401 error - full message: ${e.message}")
                    "API error (401): ${e.message ?: "Invalid API key or unauthorized"}"
                }
                e.message?.contains("402", ignoreCase = true) == true -> "API quota exceeded (402 Payment Required). Free tier limit reached."
                e.message?.contains("429", ignoreCase = true) == true -> "API quota exceeded. Try again later."
                e.message?.contains("UnknownHostException", ignoreCase = true) == true -> "No internet connection."
                else -> "Request failed: ${e.message ?: "Unknown error"}"
            }
            NetworkResult.Error(message, e)
        }
    }

    internal fun dateDaysAgo(daysBack: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysBack)
        return dateFormat.format(cal.time)
    }

    data class NewsResult(
        val articles: List<Article>,
        val totalResults: Int,
        val currentPage: Int,
        val pageSize: Int
    )

    companion object {
        @Volatile
        private var INSTANCE: NewsRepository? = null

        fun getInstance(): NewsRepository {
            return INSTANCE ?: synchronized(this) {
                NewsRepository().also { INSTANCE = it }
            }
        }
    }
}
