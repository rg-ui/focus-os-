package com.focusos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class DegreeType {
    ITEP,
    IITM
}

@Serializable
data class DegreeInfo(
    val type: DegreeType,
    val name: String,
    val currentYear: Int,
    val totalYears: Int,
    val currentCgpa: Double,
    val targetCgpa: Double,
    val completedCredits: Int,
    val totalCredits: Int,
    val currentSemester: Int,
    val isPrimaryFocus: Boolean = true
) {
    val remainingCredits: Int get() = (totalCredits - completedCredits).coerceAtLeast(1)

    fun calculateRequiredSgpa(target: Double): Double {
        val totalQualityPointsNeeded = target * totalCredits
        val currentQualityPoints = currentCgpa * completedCredits
        val remainingQualityPointsNeeded = totalQualityPointsNeeded - currentQualityPoints
        val required = remainingQualityPointsNeeded / remainingCredits
        return String.format("%.2f", required).toDouble()
    }
}

@Serializable
enum class SubjectStatus {
    NOT_STARTED,
    LEARNING,
    NEEDS_REVISION,
    EXAM_READY,
    COMPLETED
}

@Serializable
data class Subject(
    val id: String,
    val name: String,
    val degreeType: DegreeType,
    val credits: Int,
    val currentScore: Double,
    val targetScore: Double = 85.0,
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val progressPercent: Int = 45,
    val status: SubjectStatus = SubjectStatus.LEARNING,
    val nextExamDate: String? = null,
    val nextAssignmentDate: String? = null,
    val attendancePercent: Int = 82
)

@Serializable
enum class ClassAttendanceStatus {
    UPCOMING,
    PRESENT,
    ABSENT,
    WATCHED_RECORDING,
    NEED_REVISION
}

@Serializable
data class ClassSession(
    val id: String,
    val subjectName: String,
    val degreeType: DegreeType,
    val timeSlot: String,
    val topic: String,
    val status: ClassAttendanceStatus = ClassAttendanceStatus.UPCOMING,
    val isMissed: Boolean = false,
    val date: String = "Today"
)

@Serializable
data class MissedClassRecovery(
    val id: String,
    val classSessionId: String,
    val subjectName: String,
    val degreeType: DegreeType,
    val topic: String,
    val missedDate: String,
    val isRecovered: Boolean = false,
    val watchLectureDone: Boolean = false,
    val notesDone: Boolean = false,
    val quizDone: Boolean = false,
    val revisionDone: Boolean = false
) {
    val recoveryProgress: Int
        get() {
            var count = 0
            if (watchLectureDone) count++
            if (notesDone) count++
            if (quizDone) count++
            if (revisionDone) count++
            return (count * 25)
        }
}
