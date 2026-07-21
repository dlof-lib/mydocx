package com.mydocx.app.model

import com.google.gson.annotations.SerializedName

enum class ContentType { article, project }

data class Like(
    @SerializedName("user_id") val userId: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("content_id") val contentId: String
)

data class Repost(
    @SerializedName("user_id") val userId: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("content_id") val contentId: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Report(
    @SerializedName("reporter_id") val reporterId: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("content_id") val contentId: String,
    @SerializedName("reason") val reason: String
)

data class Follow(
    @SerializedName("follower_id") val followerId: String,
    @SerializedName("following_id") val followingId: String
)

data class NewsletterSubscription(
    @SerializedName("email") val email: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("active") val active: Boolean = true
)
