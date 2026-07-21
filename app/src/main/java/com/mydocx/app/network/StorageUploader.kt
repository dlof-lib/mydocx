package com.mydocx.app.network

import com.mydocx.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Uploads a file straight to Supabase Storage's REST endpoint:
 *   PUT {SUPABASE_URL}/storage/v1/object/{bucket}/{path}
 * Requires the bucket to exist and RLS/policies to allow the authenticated
 * user to write to it (see supabase/schema.sql for the storage policies).
 */
object StorageUploader {

    private val client = OkHttpClient()

    suspend fun upload(bucket: String, path: String, file: File, accessToken: String, mime: String): String? =
        withContext(Dispatchers.IO) {
            val url = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/$bucket/$path"
            val body = file.asRequestBody(mime.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .put(body)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("x-upsert", "true")
                .build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                // Public URL convention for public buckets:
                "${BuildConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/public/$bucket/$path"
            }
        }
}
