package com.locallens.api

import com.locallens.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WorldNewsApiService {

    @GET("search-news")
    suspend fun searchNews(
        @Query("source-countries") sourceCountries: String,
        @Query("language") language: String = "en",
        @Query("earliest-publish-date") earliestPublishDate: String,
        @Query("number") number: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("api-key") apiKey: String,
        @Query("category") category: String? = null
    ): NewsResponse
}
