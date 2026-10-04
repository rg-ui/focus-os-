package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class MessageSender {
    USER,
    AI
}

@Serializable
data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String? = null,
    val quickActionSuggestions: List<String> = emptyList()
)

@Serializable
data class MorningBrief(
    val greeting: String = "Good morning, Ravi.",
    val academicNotice: String = "IIT Madras Statistics assignment is due tomorrow at 11:59 PM.",
    val topPriorities: List<String> = listOf(
        "Complete IITM Statistics assignment (Week 4)",
        "Revise ITEP Mathematics — Real Analysis theorem proofs",
        "45 min Gym workout (Pull day)"
    ),
    val recommendedStudyBlock: String = "2:00 PM – 4:30 PM (Deep Work on Statistics)",
    val healthReminder: String = "Aim for 7.5h sleep tonight and 8 glasses of water."
)
