package com.intro.restaurant.data.newsapi

import com.google.gson.annotations.SerializedName
import com.intro.restaurant.data.model.NewsItem

data class NewsResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("totalResults")
    val totalResults: Int,

    @SerializedName("articles")
    val articles: List<NewsItem>
)

