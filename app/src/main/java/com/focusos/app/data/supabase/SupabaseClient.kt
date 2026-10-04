package com.focusos.app.data.supabase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseClient(
    private var supabaseUrl: String = "",
    private var anonKey: String = ""
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun updateConfig(url: String, key: String) {
        this.supabaseUrl = url.trimEnd('/')
        this.anonKey = key
    }

    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            return@withContext Result.success(true) // offline / local mode
        }
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                Result.success(response.isSuccessful || response.code in 200..399)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun invokeAiMentorEdgeFunction(
        userQuestion: String,
        userContextJson: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Supabase not configured. Operating in local intelligent offline mode."))
        }
        try {
            val payload = JSONObject().apply {
                put("question", userQuestion)
                put("context", JSONObject(userContextJson))
            }.toString()

            val request = Request.Builder()
                .url("$supabaseUrl/functions/v1/ai-mentor")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val jsonResp = JSONObject(responseBody)
                    Result.success(jsonResp.optString("reply", "No response from AI"))
                } else {
                    Result.failure(Exception("Edge function returned error ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
