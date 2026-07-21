package com.mydocx.app.network

import com.mydocx.app.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Talks directly to Supabase's auto-generated REST API (PostgREST) and GoTrue Auth API
 * over plain HTTPS + the anon/publishable key. No proprietary SDK required.
 *
 * PostgREST conventions used:
 *  - Filtering:      ?column=eq.value
 *  - Return payload:  header "Prefer: return=representation"
 *  - Upsert:          header "Prefer: resolution=merge-duplicates"
 */
interface SupabaseApi {

    // ---------- Auth (GoTrue) ----------
    @POST("auth/v1/signup")
    suspend fun signUp(@Body body: SignUpRequest): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body body: SignInRequest): Response<AuthResponse>

    // ---------- Profiles ----------
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id") idFilter: String,
        @Header("Authorization") bearer: String
    ): Response<List<Profile>>

    @GET("rest/v1/profiles")
    suspend fun getProfileByUsername(
        @Query("username") usernameFilter: String,
        @Header("Authorization") bearer: String
    ): Response<List<Profile>>

    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Query("id") idFilter: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<Profile>>

    // ---------- Articles ----------
    @GET("rest/v1/articles?select=*,author:profiles(*)&order=created_at.desc")
    suspend fun getFeedArticles(
        @Header("Authorization") bearer: String,
        @Query("limit") limit: Int = 30
    ): Response<List<Article>>

    @POST("rest/v1/articles")
    suspend fun createArticle(
        @Body body: Article,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<Article>>

    // ---------- Projects ----------
    @GET("rest/v1/projects?select=*,author:profiles(*)&order=created_at.desc")
    suspend fun getFeedProjects(
        @Header("Authorization") bearer: String,
        @Query("limit") limit: Int = 30
    ): Response<List<ProjectPost>>

    @POST("rest/v1/projects")
    suspend fun createProject(
        @Body body: ProjectPost,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<ProjectPost>>

    // ---------- Reactions ----------
    @POST("rest/v1/likes")
    suspend fun like(@Body body: Like, @Header("Authorization") bearer: String): Response<Unit>

    @DELETE("rest/v1/likes")
    suspend fun unlike(
        @Query("user_id") userId: String,
        @Query("content_id") contentId: String,
        @Header("Authorization") bearer: String
    ): Response<Unit>

    @POST("rest/v1/reposts")
    suspend fun repost(@Body body: Repost, @Header("Authorization") bearer: String): Response<Unit>

    @POST("rest/v1/reports")
    suspend fun report(@Body body: Report, @Header("Authorization") bearer: String): Response<Unit>

    @POST("rest/v1/follows")
    suspend fun follow(@Body body: Follow, @Header("Authorization") bearer: String): Response<Unit>

    @DELETE("rest/v1/follows")
    suspend fun unfollow(
        @Query("follower_id") followerId: String,
        @Query("following_id") followingId: String,
        @Header("Authorization") bearer: String
    ): Response<Unit>

    @GET("rest/v1/follows")
    suspend fun getFollowers(
        @Query("following_id") followingId: String,
        @Header("Authorization") bearer: String
    ): Response<List<Follow>>

    // ---------- Monthly email subscription ----------
    @POST("rest/v1/newsletter_subscriptions")
    suspend fun subscribeMonthly(
        @Body body: NewsletterSubscription,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Response<Unit>
}

data class SignUpRequest(val email: String, val password: String, val data: Map<String, String>)
data class SignInRequest(val email: String, val password: String)
data class AuthResponse(
    val access_token: String?,
    val refresh_token: String?,
    val user: AuthUser?
)
data class AuthUser(val id: String, val email: String?)
