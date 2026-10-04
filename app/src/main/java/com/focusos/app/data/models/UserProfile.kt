package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val userType: String = "College student",
    val onboardingCompleted: Boolean = false,
    val priorities: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    val wakeUpTime: String = "07:00 AM",
    val sleepTime: String = "11:00 PM",
    val preferredStudyHours: String = "Evenings",
    val gymPreference: String = "Evening (5 days/week)",
    val reminderStyle: String = "Balanced", // "Minimal", "Balanced", "Motivational"
    val majorGoal1: String = "CGPA Improvement & Consistency",
    val majorGoal2: String = "Career Skills & Paid Internship",
    val totalFocusedHoursThisWeek: Double = 0.0,
    val currentStreakDays: Int = 0,
    val todayScore: Int = 0
)

@Serializable
data class AppSettings(
    val themeMode: String = "dark", // "dark", "light", "system"
    val dailyMorningBriefTime: String = "07:30 AM",
    val dailyEveningCheckinTime: String = "09:30 PM",
    val maxDailyNotifications: Int = 4,
    val notificationsEnabled: Boolean = true,
    val classRemindersEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val habitRemindersEnabled: Boolean = true,
    val focusRemindersEnabled: Boolean = true,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val aiApiKey: String = ""
)

@Serializable
data class AuthSession(
    val accessToken: String = "",
    val refreshToken: String = "",
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val isLoggedIn: Boolean = false
)
