package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyJournal(
    val id: String,
    val date: String = "Today",
    val whatWentWell: String = "",
    val whatWentWrong: String = "",
    val whatToImproveTomorrow: String = "",
    val moodRating: Int = 4,
    val energyRating: Int = 4,
    val aiPatternInsight: String = ""
)

@Serializable
data class DailyReview(
    val id: String,
    val date: String = "Today",
    val completedSummary: String = "",
    val missedSummary: String = "",
    val focusedHours: Double = 3.5,
    val tomorrowPriorities: List<String> = emptyList(),
    val aiVerdict: String = ""
)
