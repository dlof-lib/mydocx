package com.mydocx.app.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the `profiles` table in Supabase (see supabase/schema.sql).
 * `id` matches the Supabase Auth user id (auth.users.id).
 */
data class Profile(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("bio") var bio: String? = null,
    @SerializedName("avatar_url") var avatarUrl: String? = null,
    @SerializedName("avatar_seed") val avatarSeed: String? = null,
    @SerializedName("special_thanks") var specialThanks: String? = null,
    @SerializedName("social_links") var socialLinks: List<SocialLink>? = null,
    @SerializedName("pin_hash") var pinHash: String? = null,
    @SerializedName("pin_salt") var pinSalt: String? = null,
    @SerializedName("followers_count") val followersCount: Int = 0,
    @SerializedName("following_count") val followingCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)

data class SocialLink(
    @SerializedName("label") val label: String,
    @SerializedName("url") val url: String
)
