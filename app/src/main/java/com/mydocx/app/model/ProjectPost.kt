package com.mydocx.app.model

import com.google.gson.annotations.SerializedName

data class ProjectPost(
    @SerializedName("id") val id: String? = null,
    @SerializedName("author_id") val authorId: String,
    @SerializedName("name") val name: String,          // e.g. "MyProject.docx"
    @SerializedName("description") val description: String,
    @SerializedName("repo_url") val repoUrl: String? = null,
    @SerializedName("storage_path") val storagePath: String? = null, // zipped structured source in Supabase Storage
    @SerializedName("language") val language: String? = null,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("reposts_count") val repostsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("author") val author: Profile? = null
)
