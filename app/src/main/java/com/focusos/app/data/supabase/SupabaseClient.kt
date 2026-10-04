package com.focusos.app.data.supabase

import com.focusos.app.data.models.AuthSession
import com.focusos.app.data.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
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
    private var currentAccessToken: String = ""

    fun updateConfig(url: String, key: String) {
        this.supabaseUrl = url.trimEnd('/')
        this.anonKey = key
    }

    fun setAccessToken(token: String) {
        this.currentAccessToken = token
    }

    private fun getAuthHeader(): String {
        return if (currentAccessToken.isNotBlank()) "Bearer $currentAccessToken" else "Bearer $anonKey"
    }

    // ----------------- Authentication -----------------
    suspend fun signUp(email: String, password: String, name: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            // Local offline mock session
            val mockId = "user_${System.currentTimeMillis()}"
            return@withContext Result.success(
                AuthSession(
                    accessToken = "local_token_$mockId",
                    refreshToken = "local_refresh_$mockId",
                    userId = mockId,
                    email = email,
                    name = name,
                    isLoggedIn = true
                )
            )
        }

        try {
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("name", name)
                })
            }.toString()

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/signup")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val accessToken = json.optString("access_token", "")
                    val refreshToken = json.optString("refresh_token", "")
                    val userObj = json.optJSONObject("user")
                    val userId = userObj?.optString("id", "") ?: json.optString("id", "")
                    val metaObj = userObj?.optJSONObject("user_metadata")
                    val userName = metaObj?.optString("name", name) ?: name

                    currentAccessToken = accessToken

                    Result.success(
                        AuthSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            userId = userId,
                            email = email,
                            name = userName,
                            isLoggedIn = true
                        )
                    )
                } else {
                    val errorMsg = parseErrorMessage(body, "Sign up failed (${response.code})")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkError(e)))
        }
    }

    suspend fun signIn(email: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            val mockId = "user_${email.hashCode()}"
            return@withContext Result.success(
                AuthSession(
                    accessToken = "local_token_$mockId",
                    refreshToken = "local_refresh_$mockId",
                    userId = mockId,
                    email = email,
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isLoggedIn = true
                )
            )
        }

        try {
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/token?grant_type=password")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val accessToken = json.optString("access_token", "")
                    val refreshToken = json.optString("refresh_token", "")
                    val userObj = json.optJSONObject("user")
                    val userId = userObj?.optString("id", "") ?: ""
                    val metaObj = userObj?.optJSONObject("user_metadata")
                    val name = metaObj?.optString("name", "") ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

                    currentAccessToken = accessToken

                    Result.success(
                        AuthSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            userId = userId,
                            email = email,
                            name = name,
                            isLoggedIn = true
                        )
                    )
                } else {
                    val errorMsg = parseErrorMessage(body, "Invalid email or password.")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkError(e)))
        }
    }

    suspend fun resetPassword(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            return@withContext Result.success(true)
        }
        try {
            val payload = JSONObject().apply {
                put("email", email)
            }.toString()

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/recover")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val body = response.body?.string() ?: ""
                    Result.failure(Exception(parseErrorMessage(body, "Password reset request failed.")))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(formatNetworkError(e)))
        }
    }

    suspend fun signOut(): Result<Boolean> = withContext(Dispatchers.IO) {
        currentAccessToken = ""
        Result.success(true)
    }

    // ----------------- Profile Sync -----------------
    suspend fun saveProfileToSupabase(profile: UserProfile): Result<Boolean> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank() || profile.id.isBlank()) {
            return@withContext Result.success(true)
        }
        try {
            val payload = JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("email", profile.email)
                put("user_type", profile.userType)
                put("onboarding_completed", profile.onboardingCompleted)
                put("priorities", JSONArray(profile.priorities))
                put("goals", JSONArray(profile.goals))
                put("challenges", JSONArray(profile.challenges))
                put("wake_up_time", profile.wakeUpTime)
                put("sleep_time", profile.sleepTime)
                put("preferred_study_hours", profile.preferredStudyHours)
                put("gym_preference", profile.gymPreference)
                put("reminder_style", profile.reminderStyle)
                put("today_score", profile.todayScore)
                put("current_streak", profile.currentStreakDays)
            }.toString()

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", getAuthHeader())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                Result.success(response.isSuccessful || response.code == 201 || response.code == 200)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProfileFromSupabase(userId: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank() || userId.isBlank()) {
            return@withContext Result.success(null)
        }
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles?id=eq.$userId&select=*")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", getAuthHeader())
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val arr = JSONArray(body)
                    if (arr.length() > 0) {
                        val obj = arr.getJSONObject(0)
                        val prioritiesList = mutableListOf<String>()
                        val prioArr = obj.optJSONArray("priorities")
                        if (prioArr != null) {
                            for (i in 0 until prioArr.length()) prioritiesList.add(prioArr.getString(i))
                        }
                        val goalsList = mutableListOf<String>()
                        val goalsArr = obj.optJSONArray("goals")
                        if (goalsArr != null) {
                            for (i in 0 until goalsArr.length()) goalsList.add(goalsArr.getString(i))
                        }
                        val challengesList = mutableListOf<String>()
                        val chalArr = obj.optJSONArray("challenges")
                        if (chalArr != null) {
                            for (i in 0 until chalArr.length()) challengesList.add(chalArr.getString(i))
                        }

                        val profile = UserProfile(
                            id = obj.optString("id", userId),
                            name = obj.optString("name", "User"),
                            email = obj.optString("email", ""),
                            userType = obj.optString("user_type", "College student"),
                            onboardingCompleted = obj.optBoolean("onboarding_completed", false),
                            priorities = prioritiesList,
                            goals = goalsList,
                            challenges = challengesList,
                            wakeUpTime = obj.optString("wake_up_time", "07:00 AM"),
                            sleepTime = obj.optString("sleep_time", "11:00 PM"),
                            preferredStudyHours = obj.optString("preferred_study_hours", "Evenings"),
                            gymPreference = obj.optString("gym_preference", "Evening (5 days/week)"),
                            reminderStyle = obj.optString("reminder_style", "Balanced"),
                            todayScore = obj.optInt("today_score", 0),
                            currentStreakDays = obj.optInt("current_streak", 0)
                        )
                        Result.success(profile)
                    } else {
                        Result.success(null)
                    }
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(body: String, fallback: String): String {
        return try {
            val json = JSONObject(body)
            json.optString("msg", json.optString("message", json.optString("error_description", fallback)))
        } catch (e: Exception) {
            fallback
        }
    }

    private fun formatNetworkError(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("Unable to resolve host", ignoreCase = true) || msg.contains("Failed to connect", ignoreCase = true) ->
                "You're offline. Please check your network connection."
            else -> "Network request failed. Please try again."
        }
    }
}
