package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class TaskCategory {
    ITEP,
    IITM,
    DATA_SCIENCE,
    INTERNSHIP,
    PROJECT,
    GATE,
    JAM,
    SSC,
    HEALTH,
    PERSONAL
}

@Serializable
enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

@Serializable
data class TaskItem(
    val id: String,
    val title: String,
    val category: TaskCategory,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val estimatedMinutes: Int = 45,
    val deadline: String = "Today",
    val isCompleted: Boolean = false,
    val isTop3: Boolean = false,
    val isCatchUpTask: Boolean = false,
    val relatedSubject: String? = null
)

@Serializable
data class FocusSession(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 25,
    val taskTitle: String = "Deep Work",
    val category: TaskCategory = TaskCategory.DATA_SCIENCE,
    val accomplishmentNotes: String = ""
)
