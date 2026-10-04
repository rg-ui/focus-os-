package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapStage(
    val stageNumber: Int,
    val title: String,
    val description: String,
    val progressPercent: Int,
    val estimatedHours: Int,
    val lessonsCompleted: Int,
    val totalLessons: Int,
    val practiceProblemsDone: Int,
    val totalPracticeProblems: Int,
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false
)

@Serializable
data class ProjectTask(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

@Serializable
data class PortfolioProject(
    val id: String,
    val title: String,
    val description: String,
    val goal: String,
    val techStack: List<String>,
    val githubUrl: String = "",
    val status: String = "In Progress", // Not Started, In Progress, Completed
    val progressPercent: Int = 40,
    val tasks: List<ProjectTask> = emptyList()
)

@Serializable
enum class InternshipStatus {
    SAVED,
    APPLIED,
    SHORTLISTED,
    INTERVIEW,
    OFFER,
    REJECTED
}

@Serializable
data class InternshipApplication(
    val id: String,
    val company: String,
    val role: String,
    val stipend: String = "₹6,000/month",
    val location: String = "Remote",
    val appliedDate: String = "2026-10-01",
    val followUpDate: String = "2026-10-08",
    val status: InternshipStatus = InternshipStatus.APPLIED,
    val link: String = "",
    val notes: String = ""
)

@Serializable
data class ExamTrack(
    val name: String, // GATE, JAM, SSC
    val paper: String,
    val status: String, // "Exploration", "Future Option", "Active"
    val syllabusProgress: Int,
    val pyqCompleted: Int,
    val totalPyqs: Int,
    val targetYear: String,
    val targetInstitutes: String,
    val isActive: Boolean = false
)
