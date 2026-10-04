package com.focusos.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.models.*
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.CategoryBadge
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
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

    GlassBackgroundBox {
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
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = GlassDarkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dual-Degree CGPA Recovery & Mastery System",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassDarkTextSecondary
                    )
                }
            }

            // 2. ITEP CARD
            if (itep != null) {
                item {
                    GlassDegreeCgpaCard(
                        degree = itep,
                        accentColor = AccentPurple,
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
                    GlassDegreeCgpaCard(
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
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = GlassDarkTextSecondary
                        )
                        Text(
                            text = if (unrecoveredCount == 0) "All caught up" else "$unrecoveredCount sessions to recover",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (unrecoveredCount == 0) StatusGreen else StatusOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (missedRecoveries.isEmpty()) {
                        GlassCard {
                            Text(
                                text = "Zero missed sessions! Your attendance discipline is exceptional.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassDarkTextSecondary
                            )
                        }
                    } else {
                        missedRecoveries.forEach { recovery ->
                            GlassCard(
                                backgroundColor = if (recovery.isRecovered) Color(0x10FFFFFF) else StatusOrangeSubtle
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${recovery.subjectName}: ${recovery.topic}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = GlassDarkTextPrimary
                                        )
                                        Text(
                                            text = if (recovery.isRecovered) "Recovered ✅" else "Pending",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (recovery.isRecovered) StatusGreen else StatusOrange
                                        )
                                    }

                                    // Checklist
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RecoveryChip("Watch Lecture", recovery.watchLectureDone) {
                                            repository.toggleRecoveryStep(recovery.id, 0)
                                        }
                                        RecoveryChip("Notes", recovery.notesDone) {
                                            repository.toggleRecoveryStep(recovery.id, 1)
                                        }
                                        RecoveryChip("Quiz", recovery.quizDone) {
                                            repository.toggleRecoveryStep(recovery.id, 2)
                                        }
                                        RecoveryChip("Revision", recovery.revisionDone) {
                                            repository.toggleRecoveryStep(recovery.id, 3)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // 5. SUBJECT STATUS & SCORES
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SEMESTER SUBJECTS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = GlassDarkTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DegreeFilterChip("All", selectedDegreeFilter == null) { selectedDegreeFilter = null }
                            DegreeFilterChip("ITEP", selectedDegreeFilter == DegreeType.ITEP) { selectedDegreeFilter = DegreeType.ITEP }
                            DegreeFilterChip("IITM", selectedDegreeFilter == DegreeType.IITM) { selectedDegreeFilter = DegreeType.IITM }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassCard {
                        filteredSubjects.forEachIndexed { index, subject ->
                            SubjectRow(subject = subject)
                            if (index < filteredSubjects.size - 1) {
                                HorizontalDivider(
                                    color = Color(0x15FFFFFF),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassDegreeCgpaCard(
    degree: DegreeInfo,
    accentColor: Color,
    targetOptions: List<Double>,
    onTargetSelected: (Double) -> Unit
) {
    val progress = (degree.currentCgpa / 10.0).toFloat().coerceIn(0f, 1f)

    GlassCard(
        borderGradient = Brush.linearGradient(
            listOf(accentColor.copy(alpha = 0.5f), Color(0x15FFFFFF), Color(0x05FFFFFF))
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = degree.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassDarkTextPrimary
                    )
                    Text(
                        text = "Year ${degree.currentYear} of ${degree.totalYears} • Semester ${degree.currentSemester}",
                        fontSize = 12.sp,
                        color = GlassDarkTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Active Focus",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            // CGPA numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("CURRENT CGPA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextTertiary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${degree.currentCgpa}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                        Text(" / 10.0", fontSize = 13.sp, color = GlassDarkTextSecondary, modifier = Modifier.padding(bottom = 3.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("TARGET CGPA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextTertiary)
                    Text("${degree.targetCgpa}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }
            }

            // Progress bar
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = Color(0x18FFFFFF)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${degree.completedCredits} / ${degree.totalCredits} Credits Done", fontSize = 11.sp, color = GlassDarkTextSecondary)
                    Text("Req SGPA: ~${degree.calculateRequiredSgpa(degree.targetCgpa)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
                }
            }

            // Target selector pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Set Target:", fontSize = 11.sp, color = GlassDarkTextTertiary)
                targetOptions.forEach { opt ->
                    val isSel = degree.targetCgpa == opt
                    val pillShape = RoundedCornerShape(8.dp)
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(if (isSel) accentColor.copy(alpha = 0.25f) else Color(0x10FFFFFF))
                            .border(1.dp, if (isSel) accentColor.copy(alpha = 0.7f) else Color(0x15FFFFFF), pillShape)
                            .clickable { onTargetSelected(opt) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$opt",
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) accentColor else GlassDarkTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryChip(
    label: String,
    isDone: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isDone) StatusGreen.copy(alpha = 0.2f) else Color(0x12FFFFFF))
            .border(1.dp, if (isDone) StatusGreen.copy(alpha = 0.5f) else Color(0x18FFFFFF), shape)
            .clickable { onToggle() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isDone) "$label ✓" else label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDone) StatusGreen else GlassDarkTextSecondary
        )
    }
}

@Composable
private fun DegreeFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) AccentBlue.copy(alpha = 0.25f) else Color(0x10FFFFFF))
            .border(1.dp, if (isSelected) AccentBlue.copy(alpha = 0.5f) else Color(0x15FFFFFF), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentCyan else GlassDarkTextSecondary
        )
    }
}

@Composable
private fun SubjectRow(subject: Subject) {
    val (statusBg, statusBorder, statusText) = when (subject.status) {
        SubjectStatus.COMPLETED, SubjectStatus.EXAM_READY -> Triple(StatusGreenSubtle, StatusGreen.copy(alpha = 0.4f), StatusGreen)
        SubjectStatus.LEARNING -> Triple(StatusBlueSubtle, AccentBlue.copy(alpha = 0.4f), AccentBlue)
        SubjectStatus.NEEDS_REVISION -> Triple(StatusOrangeSubtle, StatusOrange.copy(alpha = 0.4f), StatusOrange)
        SubjectStatus.NOT_STARTED -> Triple(Color(0x14FFFFFF), Color(0x20FFFFFF), GlassDarkTextSecondary)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subject.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${subject.credits} Credits • Difficulty: ${subject.difficulty}",
                fontSize = 11.sp,
                color = GlassDarkTextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val shape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(statusBg)
                    .border(1.dp, statusBorder, shape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = subject.status.name.replace("_", " "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusText
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${subject.currentScore.toInt()}% (Target ${subject.targetScore.toInt()}%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassDarkTextSecondary
            )
        }
    }
}
