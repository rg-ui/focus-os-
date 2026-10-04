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
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*

@Composable
fun CareerScreen(
    repository: FocusOsRepository,
    onNavigateToAiMentor: (String) -> Unit
) {
    val roadmap by repository.dsRoadmap.collectAsState()
    val projects by repository.portfolioProjects.collectAsState()
    val internships by repository.internships.collectAsState()
    val exams by repository.examTracks.collectAsState()

    var showAddInternshipDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("DS Roadmap") }

    val activeProject = projects.firstOrNull()

    // Analytics calculations
    val totalApplied = internships.size
    val totalResponses = internships.count { it.status != InternshipStatus.SAVED && it.status != InternshipStatus.APPLIED }
    val totalInterviews = internships.count { it.status == InternshipStatus.INTERVIEW || it.status == InternshipStatus.OFFER }
    val responseRate = if (totalApplied > 0) (totalResponses * 100) / totalApplied else 0
    val interviewRate = if (totalApplied > 0) (totalInterviews * 100) / totalApplied else 0

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Career & Skills",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = GlassDarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Data Science Roadmap & ₹5k-6k Internship Goal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassDarkTextSecondary
                        )
                    }
                }
            }

            // 2. NAVIGATION TABS
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DS Roadmap", "Mini Project", "Internships", "GATE / JAM").forEach { tab ->
                        val isSel = selectedTab == tab
                        val shape = RoundedCornerShape(12.dp)
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(shape)
                                    .background(if (isSel) AccentBlue.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                                    .border(
                                        1.dp,
                                        if (isSel) AccentBlue.copy(alpha = 0.6f) else Color(0x18FFFFFF),
                                        shape
                                    )
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) AccentCyan else GlassDarkTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // TAB CONTENT
            when (selectedTab) {
                "DS Roadmap" -> {
                    item {
                        val totalProgress = if (roadmap.isNotEmpty()) roadmap.sumOf { it.progressPercent } / roadmap.size else 0
                        val completedCount = roadmap.count { it.isCompleted }

                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("9-STAGE DS ROADMAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Overall Progress: $totalProgress%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StatusGreenSubtle)
                                        .border(1.dp, StatusGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("$completedCount/9 Done", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusGreen)
                                }
                            }
                        }
                    }

                    items(roadmap) { stage ->
                        RoadmapStageGlassCard(
                            stage = stage,
                            onProgressChange = { newProg, lessons, problems ->
                                repository.updateRoadmapStageProgress(stage.stageNumber, newProg, lessons, problems)
                            }
                        )
                    }
                }

                "Mini Project" -> {
                    if (activeProject != null) {
                        item {
                            GlassCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeProject.title,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GlassDarkTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = activeProject.description,
                                            fontSize = 12.sp,
                                            color = GlassDarkTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Tech Stack Pills
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(activeProject.techStack) { tech ->
                                        val shape = RoundedCornerShape(6.dp)
                                        Box(
                                            modifier = Modifier
                                                .clip(shape)
                                                .background(Color(0x15FFFFFF))
                                                .border(1.dp, Color(0x25FFFFFF), shape)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(text = tech, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = AccentCyan)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Progress Bar
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Project Checklist", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                                        Text("${activeProject.progressPercent}% Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (activeProject.progressPercent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = AccentBlue,
                                        trackColor = Color(0x18FFFFFF)
                                    )
                                }
                            }
                        }

                        // Project Task Checklist Items
                        item {
                            Text(
                                text = "STEP-BY-STEP CHECKLIST",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = GlassDarkTextSecondary
                            )
                        }

                        items(activeProject.tasks) { task ->
                            GlassCard(
                                contentPadding = 12.dp,
                                onClick = { repository.toggleProjectTask(activeProject.id, task.id) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (task.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Check",
                                        tint = if (task.isDone) StatusGreen else GlassDarkTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium,
                                        color = if (task.isDone) GlassDarkTextSecondary else GlassDarkTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                "Internships" -> {
                    item {
                        GlassCard {
                            Text("INTERNSHIP FUNNEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$totalApplied", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                                    Text("Applied", fontSize = 11.sp, color = GlassDarkTextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$responseRate%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                    Text("Response Rate", fontSize = 11.sp, color = GlassDarkTextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$totalInterviews", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                                    Text("Interviews", fontSize = 11.sp, color = GlassDarkTextSecondary)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "APPLICATIONS (${internships.size})",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = GlassDarkTextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusBlueSubtle)
                                    .border(1.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { showAddInternshipDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ Add Application", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AccentBlue)
                            }
                        }
                    }

                    items(internships) { application ->
                        InternshipGlassCard(
                            application = application,
                            onStatusChange = { newStatus ->
                                repository.updateInternshipStatus(application.id, newStatus)
                            }
                        )
                    }
                }

                "GATE / JAM" -> {
                    item {
                        GlassCard(
                            backgroundColor = AccentPurple.copy(alpha = 0.08f),
                            borderGradient = Brush.linearGradient(listOf(AccentPurple.copy(alpha = 0.4f), Color(0x10FFFFFF)))
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = "Note", tint = AccentPurple, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Strategy Priority Reminder", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Focus OS recommends prioritizing ITEP & IITM CGPA recovery + paid internship before dedicating full energy to competitive exams.",
                                        fontSize = 11.sp,
                                        color = GlassDarkTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    items(exams) { exam ->
                        ExamGlassCard(
                            exam = exam,
                            onAskAi = { onNavigateToAiMentor("How should I balance ${exam.name} preparation with my current semester?") }
                        )
                    }
                }
            }
        }
    }

    if (showAddInternshipDialog) {
        AddInternshipDialog(
            onDismiss = { showAddInternshipDialog = false },
            onAdd = { newApp ->
                repository.addInternship(newApp)
                showAddInternshipDialog = false
            }
        )
    }
}

@Composable
private fun RoadmapStageGlassCard(
    stage: RoadmapStage,
    onProgressChange: (Int, Int, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard(
        backgroundColor = if (stage.isCompleted) Color(0x12FFFFFF) else Color(0x18FFFFFF),
        borderGradient = if (stage.isCompleted) Brush.linearGradient(listOf(StatusGreen.copy(alpha = 0.4f), Color(0x10FFFFFF))) else GlassBorderGradient
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (stage.isCompleted) StatusGreenSubtle else StatusBlueSubtle)
                            .border(1.dp, if (stage.isCompleted) StatusGreen else AccentBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stage.stageNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stage.isCompleted) StatusGreen else AccentBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stage.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassDarkTextPrimary
                        )
                        Text(
                            text = "~${stage.estimatedHours} hours",
                            fontSize = 11.sp,
                            color = GlassDarkTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (stage.isCompleted) StatusGreenSubtle else Color(0x15FFFFFF))
                        .border(1.dp, if (stage.isCompleted) StatusGreen.copy(alpha = 0.4f) else Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (stage.isCompleted) "Completed ✓" else "${stage.progressPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stage.isCompleted) StatusGreen else AccentCyan
                    )
                }
            }

            Text(
                text = stage.description,
                fontSize = 12.sp,
                color = GlassDarkTextSecondary
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { (stage.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (stage.isCompleted) StatusGreen else AccentBlue,
                trackColor = Color(0x18FFFFFF)
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lessons: ${stage.lessonsCompleted}/${stage.totalLessons} • Problems: ${stage.practiceProblemsDone}/${stage.totalPracticeProblems}",
                        fontSize = 11.sp,
                        color = GlassDarkTextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val pillShape = RoundedCornerShape(6.dp)
                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(StatusBlueSubtle)
                                .border(1.dp, AccentBlue.copy(alpha = 0.3f), pillShape)
                                .clickable {
                                    val nextProg = (stage.progressPercent + 25).coerceAtMost(100)
                                    val nextLess = (stage.lessonsCompleted + 2).coerceAtMost(stage.totalLessons)
                                    val nextProb = (stage.practiceProblemsDone + 5).coerceAtMost(stage.totalPracticeProblems)
                                    onProgressChange(nextProg, nextLess, nextProb)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("+25% Prog", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(if (stage.isCompleted) Color(0x15FFFFFF) else StatusGreenSubtle)
                                .border(1.dp, if (stage.isCompleted) Color(0x25FFFFFF) else StatusGreen.copy(alpha = 0.4f), pillShape)
                                .clickable {
                                    if (stage.isCompleted) {
                                        onProgressChange(0, 0, 0)
                                    } else {
                                        onProgressChange(100, stage.totalLessons, stage.totalPracticeProblems)
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (stage.isCompleted) "Reset" else "Mark Done", fontSize = 10.sp, color = if (stage.isCompleted) GlassDarkTextSecondary else StatusGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InternshipGlassCard(
    application: InternshipApplication,
    onStatusChange: (InternshipStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${application.role} • ${application.company}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassDarkTextPrimary
                )
                Text(
                    text = "${application.stipend} • ${application.location} • Applied: ${application.appliedDate}",
                    fontSize = 12.sp,
                    color = GlassDarkTextSecondary
                )
                if (application.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = application.notes,
                        fontSize = 11.sp,
                        color = AccentCyan
                    )
                }
            }

            Box {
                val shape = RoundedCornerShape(8.dp)
                val statusColor = when (application.status) {
                    InternshipStatus.OFFER -> StatusGreen
                    InternshipStatus.INTERVIEW -> StatusTeal
                    InternshipStatus.SHORTLISTED -> AccentBlue
                    InternshipStatus.APPLIED -> GlassDarkTextPrimary
                    InternshipStatus.REJECTED -> StatusRed
                    InternshipStatus.SAVED -> GlassDarkTextSecondary
                }

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), shape)
                        .clickable { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = application.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    InternshipStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
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

@Composable
private fun ExamGlassCard(
    exam: ExamTrack,
    onAskAi: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${exam.name} — ${exam.paper}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassDarkTextPrimary
                )
                Text(
                    text = "Status: ${exam.status} • Target: ${exam.targetYear}",
                    fontSize = 12.sp,
                    color = GlassDarkTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusPurpleSubtle)
                    .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${exam.syllabusProgress}% Syllabus",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Solved ${exam.pyqCompleted}/${exam.totalPyqs} Previous Year Questions",
            fontSize = 12.sp,
            color = GlassDarkTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x12FFFFFF))
                .border(1.dp, Brush.linearGradient(listOf(AccentPurple.copy(alpha = 0.3f), Color(0x10FFFFFF))), RoundedCornerShape(10.dp))
                .clickable(onClick = onAskAi)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = "AI", tint = AccentPurple, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ask AI: Balance ${exam.name} with CGPA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
            }
        }
    }
}

@Composable
fun AddInternshipDialog(
    onDismiss: () -> Unit,
    onAdd: (InternshipApplication) -> Unit
) {
    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var stipend by remember { mutableStateOf("₹6,000/month") }
    var location by remember { mutableStateOf("Remote") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Internship Application", color = GlassDarkTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stipend,
                        onValueChange = { stipend = it },
                        label = { Text("Stipend") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (company.isNotBlank() && role.isNotBlank()) {
                        onAdd(
                            InternshipApplication(
                                id = "intern_${System.currentTimeMillis()}",
                                company = company.trim(),
                                role = role.trim(),
                                stipend = stipend.ifBlank { "₹6,000/month" },
                                location = location.ifBlank { "Remote" }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GlassDarkTextSecondary)
            }
        }
    )
}
