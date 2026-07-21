package com.mydocx.app.network

import com.mydocx.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Every Supabase REST/Auth call needs the anon/publishable key as apikey header.
    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(logging)
        .build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }

    /** Standard bearer header. Uses the anon key before login, the user JWT after. */
    fun bearer(token: String?): String = "Bearer ${token ?: BuildConfig.SUPABASE_ANON_KEY}"
}
