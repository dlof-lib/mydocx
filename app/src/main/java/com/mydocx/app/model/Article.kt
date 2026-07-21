package com.mydocx.app.model

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("id") val id: String? = null,
    @SerializedName("author_id") val authorId: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("reposts_count") val repostsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
    // populated client-side by joining profiles, not a real DB column
    @SerializedName("author") val author: Profile? = null
)
