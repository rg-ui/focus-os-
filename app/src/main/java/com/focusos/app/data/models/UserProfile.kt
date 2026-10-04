package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "user_ravi_1",
    val name: String = "Ravi",
    val email: String = "ravi@focusos.app",
    val onboardingCompleted: Boolean = true,
    val majorGoal1: String = "CGPA Recovery (ITEP -> 7.5+, IITM -> 6.5+)",
    val majorGoal2: String = "Data Science Foundation & Paid Internship (₹5k-6k/mo)",
    val totalFocusedHoursThisWeek: Double = 14.5,
    val currentStreakDays: Int = 7,
    val todayScore: Int = 72
)

@Serializable
data class AppSettings(
    val themeMode: String = "system", // "dark", "light", "system"
    val dailyMorningBriefTime: String = "07:30 AM",
    val dailyEveningCheckinTime: String = "09:30 PM",
    val maxDailyNotifications: Int = 4,
    val notificationsEnabled: Boolean = true,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val aiApiKey: String = ""
)
