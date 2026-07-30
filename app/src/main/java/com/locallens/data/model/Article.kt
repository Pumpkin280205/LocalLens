package com.locallens.data.model

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("text")
    val fullText: String? = null,

    @SerializedName("summary")
    val summary: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("image")
    val imageUrl: String? = null,

    @SerializedName("video")
    val videoUrl: String? = null,

    @SerializedName("publish_date")
    val publicationDate: String? = null,

    @SerializedName("author")
    val authors: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("language")
    val language: String? = null,

    @SerializedName("source_country")
    val sourceCountry: String? = null,

    @SerializedName("sentiment")
    val sentiment: String? = null
)
