package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class WorkoutType {
    PUSH,
    PULL,
    LEGS,
    CARDIO,
    REST,
    OTHER
}

@Serializable
data class HealthLog(
    val date: String = "Today",
    val gymCompleted: Boolean = true,
    val workoutType: WorkoutType = WorkoutType.PULL,
    val workoutDurationMinutes: Int = 45,
    val steps: Int = 6420,
    val sleepHours: Double = 7.2,
    val waterGlasses: Int = 8,
    val moodRating: Int = 4, // 1 to 5
    val energyRating: Int = 4 // 1 to 5
)

@Serializable
data class HabitItem(
    val id: String,
    val name: String,
    val iconName: String,
    val currentStreak: Int,
    val completedToday: Boolean,
    val last7Days: List<Boolean> // true = completed, false = missed
)

@Serializable
data class DistractionLog(
    val date: String = "Today",
    val instagramMinutes: Int = 25,
    val youtubeMinutes: Int = 65,
    val gamingMinutes: Int = 0,
    val otherMinutes: Int = 40,
    val yesterdayTotalMinutes: Int = 165
) {
    val todayTotalMinutes: Int get() = instagramMinutes + youtubeMinutes + gamingMinutes + otherMinutes
    val diffMinutes: Int get() = yesterdayTotalMinutes - todayTotalMinutes
    val isImprovement: Boolean get() = diffMinutes >= 0
}
