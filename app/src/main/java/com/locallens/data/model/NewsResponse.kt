package com.locallens.data.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("available")
    val totalResults: Int? = null,

    @SerializedName("news")
    val articles: List<Article> = emptyList()
)
