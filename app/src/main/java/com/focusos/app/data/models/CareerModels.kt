package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapStage(
    val stageNumber: Int,
    val title: String,
    val description: String,
    val progressPercent: Int = 0,
    val estimatedHours: Int = 20,
    val lessonsCompleted: Int = 0,
    val totalLessons: Int = 10,
    val practiceProblemsDone: Int = 0,
    val totalPracticeProblems: Int = 25,
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
    val techStack: List<String> = emptyList(),
    val githubUrl: String = "",
    val status: String = "In Progress", // Not Started, In Progress, Completed
    val progressPercent: Int = 0,
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
    val stipend: String = "Competitive / Performance",
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
    val paper: String = "Computer Science / Data Science",
    val status: String = "Active", // "Exploration", "Future Option", "Active"
    val syllabusProgress: Int = 0,
    val pyqCompleted: Int = 0,
    val totalPyqs: Int = 100,
    val targetYear: String = "2027",
    val targetInstitutes: String = "Top Institutions",
    val isActive: Boolean = true
)

@Serializable
data class ExamGoal(
    val examName: String,
    val targetYear: Int = 2027,
    val targetPercentileOrRank: String = "Top 1%",
    val syllabusProgressPercent: Int = 0,
    val mockTestsGiven: Int = 0,
    val averageScore: Double = 0.0
)
