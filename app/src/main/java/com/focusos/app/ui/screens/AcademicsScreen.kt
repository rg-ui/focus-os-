package com.focusos.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.models.*
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.AppleCard
import com.focusos.app.ui.components.CategoryBadge
import com.focusos.app.ui.theme.*

@Composable
fun AcademicsScreen(
    repository: FocusOsRepository
) {
    val degrees by repository.degrees.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val missedRecoveries by repository.missedRecoveries.collectAsState()

    val itep = degrees.find { it.type == DegreeType.ITEP }
    val iitm = degrees.find { it.type == DegreeType.IITM }

    var selectedDegreeFilter by remember { mutableStateOf<DegreeType?>(null) }
    val filteredSubjects = remember(selectedDegreeFilter, subjects) {
        if (selectedDegreeFilter == null) subjects
        else subjects.filter { it.degreeType == selectedDegreeFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. HEADER
        item {
            Column {
                Text(
                    text = "Academics",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Dual-Degree CGPA Recovery & Mastery System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. ITEP CARD
        if (itep != null) {
            item {
                DegreeCgpaCard(
                    degree = itep,
                    accentColor = StatusPurple,
                    targetOptions = listOf(7.0, 7.5, 8.0, 8.5),
                    onTargetSelected = { target ->
                        repository.updateDegree(itep.type, itep.currentCgpa, target, itep.completedCredits)
                    }
                )
            }
        }

        // 3. IITM CARD
        if (iitm != null) {
            item {
                DegreeCgpaCard(
                    degree = iitm,
                    accentColor = AccentBlue,
                    targetOptions = listOf(6.0, 6.5, 7.0, 7.5, 8.0),
                    onTargetSelected = { target ->
                        repository.updateDegree(iitm.type, iitm.currentCgpa, target, iitm.completedCredits)
                    }
                )
            }
        }

        // 4. MISSED CLASS RECOVERY SECTION
        item {
            Column {
                val unrecoveredCount = missedRecoveries.count { !it.isRecovered }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MISSED CLASS RECOVERY",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (unrecoveredCount == 0) "All caught up" else "$unrecoveredCount sessions to recover",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unrecoveredCount == 0) StatusGreen else StatusOrange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (missedRecoveries.isEmpty()) {
                    AppleCard {
                        Text(
                            text = "No missed classes! Attendance is completely on track.",
                            fontSize = 13.sp,
                            color = StatusGreen
                        )
                    }
                } else {
                    missedRecoveries.forEach { recovery ->
                        MissedClassRecoveryCard(
                            recovery = recovery,
                            onToggleStep = { stepIndex ->
                                repository.toggleRecoveryStep(recovery.id, stepIndex)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // 5. SUBJECT TRACKER & ACADEMIC HEALTH
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBJECT TRACKER",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Degree filter tabs
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(null to "All", DegreeType.ITEP to "ITEP", DegreeType.IITM to "IITM").forEach { (type, label) ->
                            val isSel = selectedDegreeFilter == type
                            Surface(
                                onClick = { selectedDegreeFilter = type },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) AccentBlue else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                filteredSubjects.forEach { subject ->
                    SubjectRowCard(
                        subject = subject,
                        onStatusChange = { newStatus ->
                            repository.updateSubjectStatus(subject.id, newStatus)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun DegreeCgpaCard(
    degree: DegreeInfo,
    accentColor: Color,
    targetOptions: List<Double>,
    onTargetSelected: (Double) -> Unit
) {
    val requiredSgpa = degree.calculateRequiredSgpa(degree.targetCgpa)
    val isAchievable = requiredSgpa <= 10.0

    AppleCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = degree.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Year ${degree.currentYear} • Semester ${degree.currentSemester} • ${degree.completedCredits}/${degree.totalCredits} Credits",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${degree.currentCgpa} CGPA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Selector Buttons
        Text(
            text = "Select Target CGPA:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(targetOptions) { target ->
                val isSelected = degree.targetCgpa == target
                Surface(
                    onClick = { onTargetSelected(target) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$target",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Required SGPA Projection Box
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isAchievable) StatusGreenSubtle else StatusOrangeSubtle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isAchievable)
                        "To reach ${degree.targetCgpa} CGPA, you need approx $requiredSgpa SGPA over the remaining ${degree.remainingCredits} credits."
                    else
                        "Target of ${degree.targetCgpa} would require $requiredSgpa SGPA (> 10.0). Consider adjusting target.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAchievable) StatusGreen else StatusOrange
                )
            }
        }
    }
}

@Composable
private fun MissedClassRecoveryCard(
    recovery: MissedClassRecovery,
    onToggleStep: (Int) -> Unit
) {
    AppleCard(
        borderColor = if (recovery.isRecovered) StatusGreen.copy(alpha = 0.4f) else StatusOrange.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBadge(
                        category = if (recovery.degreeType == DegreeType.IITM) TaskCategory.IITM else TaskCategory.ITEP
                    )
                    Text(
                        text = "Missed: ${recovery.missedDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recovery.subjectName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = recovery.topic,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (recovery.isRecovered) StatusGreenSubtle else StatusOrangeSubtle
            ) {
                Text(
                    text = if (recovery.isRecovered) "Recovered" else "${recovery.recoveryProgress}% Done",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (recovery.isRecovered) StatusGreen else StatusOrange,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Recovery Steps
        Text(
            text = "Recovery Checklist:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        val steps = listOf(
            "1. Watch lecture recording" to recovery.watchLectureDone,
            "2. Complete lecture notes" to recovery.notesDone,
            "3. Attempt practice quiz" to recovery.quizDone,
            "4. Revise formulas & concepts" to recovery.revisionDone
        )

        steps.forEachIndexed { index, (label, isDone) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleStep(index) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggleStep(index) },
                    colors = CheckboxDefaults.colors(checkedColor = StatusGreen)
                )
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SubjectRowCard(
    subject: Subject,
    onStatusChange: (SubjectStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AppleCard(contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBadge(
                        category = if (subject.degreeType == DegreeType.IITM) TaskCategory.IITM else TaskCategory.ITEP
                    )
                    Text(
                        text = "${subject.credits} Credits • Difficulty: ${subject.difficulty}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subject.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subject.nextAssignmentDate != null) {
                    Text(
                        text = "Next Assignment: ${subject.nextAssignmentDate}",
                        fontSize = 12.sp,
                        color = StatusRed,
                        fontWeight = FontWeight.Medium
                    )
                } else if (subject.nextExamDate != null) {
                    Text(
                        text = "Next Exam: ${subject.nextExamDate}",
                        fontSize = 12.sp,
                        color = AccentBlue
                    )
                }
            }

            Box {
                Surface(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = when (subject.status) {
                        SubjectStatus.EXAM_READY -> StatusGreenSubtle
                        SubjectStatus.NEEDS_REVISION -> StatusOrangeSubtle
                        SubjectStatus.LEARNING -> StatusBlueSubtle
                        SubjectStatus.COMPLETED -> StatusGreenSubtle
                        SubjectStatus.NOT_STARTED -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject.status.name.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (subject.status) {
                                SubjectStatus.EXAM_READY -> StatusGreen
                                SubjectStatus.NEEDS_REVISION -> StatusOrange
                                SubjectStatus.LEARNING -> AccentBlue
                                SubjectStatus.COMPLETED -> StatusGreen
                                SubjectStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Menu",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    SubjectStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name.replace("_", " ")) },
                            onClick = {
                                onStatusChange(status)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
