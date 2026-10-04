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
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Data Science Roadmap & ₹5k-6k Internship Goal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. NAVIGATION TABS
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DS Roadmap", "Mini Project", "Internships", "GATE / JAM").forEach { tab ->
                    val isSel = selectedTab == tab
                    item {
                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) AccentBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. TAB CONTENT
        when (selectedTab) {
            "DS Roadmap" -> {
                item {
                    AppleCard(
                        backgroundColor = StatusBlueSubtle,
                        borderColor = AccentBlue.copy(alpha = 0.3f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = "Goal", tint = AccentBlue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Target: Paid Internship (₹5,000–₹6,000/mo)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AccentBlue
                                )
                                Text(
                                    text = "Focus: Python → Pandas → SQL → Portfolio EDA Project",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(roadmap) { stage ->
                    RoadmapStageCard(stage = stage)
                }
            }

            "Mini Project" -> {
                if (activeProject != null) {
                    item {
                        AppleCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeProject.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeProject.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusBlueSubtle
                                ) {
                                    Text(
                                        text = "${activeProject.progressPercent}%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tech stack badges
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                activeProject.techStack.forEach { tech ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = tech,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { activeProject.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AccentBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Project Tasks (${activeProject.tasks.count { it.isDone }}/${activeProject.tasks.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            activeProject.tasks.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { repository.toggleProjectTask(activeProject.id, task.id) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isDone,
                                        onCheckedChange = { repository.toggleProjectTask(activeProject.id, task.id) },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                                    )
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "Internships" -> {
                // Analytics Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppleCard(modifier = Modifier.weight(1f), contentPadding = 12.dp) {
                            Text("Total Apps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalApplied", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        AppleCard(modifier = Modifier.weight(1f), contentPadding = 12.dp) {
                            Text("Response %", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$responseRate%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                        }
                        AppleCard(modifier = Modifier.weight(1f), contentPadding = 12.dp) {
                            Text("Interview %", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$interviewRate%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusTeal)
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
                            text = "APPLICATIONS PIPELINE",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { showAddInternshipDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(StatusGreenSubtle)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = StatusGreen)
                        }
                    }
                }

                items(internships) { application ->
                    InternshipCard(
                        application = application,
                        onStatusChange = { newStatus ->
                            repository.updateInternshipStatus(application.id, newStatus)
                        }
                    )
                }
            }

            "GATE / JAM" -> {
                item {
                    AppleCard(
                        backgroundColor = StatusPurpleSubtle,
                        borderColor = StatusPurple.copy(alpha = 0.3f)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Notice", tint = StatusPurple, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Exploration Phase (No Overplanning)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = StatusPurple
                                )
                                Text(
                                    text = "GATE/JAM are kept in exploration mode to protect your dual-degree CGPA and internship target. Heavy prep will activate when you decide.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(exams) { exam ->
                    ExamCard(
                        exam = exam,
                        onAskAi = {
                            onNavigateToAiMentor("Should I focus on ${exam.name} or internship given my current CGPA?")
                        }
                    )
                }
            }
        }
    }

    if (showAddInternshipDialog) {
        AddInternshipDialog(
            onDismiss = { showAddInternshipDialog = false },
            onAdd = { app ->
                repository.addInternship(app)
                showAddInternshipDialog = false
            }
        )
    }
}

@Composable
private fun RoadmapStageCard(stage: RoadmapStage) {
    AppleCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (stage.isCompleted) StatusGreenSubtle else if (stage.isCurrent) StatusBlueSubtle else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stage.stageNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (stage.isCompleted) StatusGreen else if (stage.isCurrent) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stage.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stage.lessonsCompleted}/${stage.totalLessons} lessons • ${stage.practiceProblemsDone}/${stage.totalPracticeProblems} problems",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (stage.isCompleted) StatusGreenSubtle else if (stage.isCurrent) StatusBlueSubtle else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (stage.isCompleted) "Completed" else if (stage.isCurrent) "Current (${stage.progressPercent}%)" else "${stage.progressPercent}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (stage.isCompleted) StatusGreen else if (stage.isCurrent) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { stage.progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (stage.isCompleted) StatusGreen else AccentBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun InternshipCard(
    application: InternshipApplication,
    onStatusChange: (InternshipStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AppleCard(contentPadding = 14.dp) {
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${application.stipend} • ${application.location} • Applied: ${application.appliedDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (application.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = application.notes,
                        fontSize = 11.sp,
                        color = AccentBlue
                    )
                }
            }

            Box {
                Surface(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = when (application.status) {
                        InternshipStatus.OFFER -> StatusGreenSubtle
                        InternshipStatus.INTERVIEW -> StatusTeal.copy(alpha = 0.15f)
                        InternshipStatus.SHORTLISTED -> StatusBlueSubtle
                        InternshipStatus.APPLIED -> MaterialTheme.colorScheme.surfaceVariant
                        InternshipStatus.REJECTED -> StatusRedSubtle
                        InternshipStatus.SAVED -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = application.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (application.status) {
                            InternshipStatus.OFFER -> StatusGreen
                            InternshipStatus.INTERVIEW -> StatusTeal
                            InternshipStatus.SHORTLISTED -> AccentBlue
                            InternshipStatus.APPLIED -> MaterialTheme.colorScheme.onSurface
                            InternshipStatus.REJECTED -> StatusRed
                            InternshipStatus.SAVED -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
private fun ExamCard(
    exam: ExamTrack,
    onAskAi: () -> Unit
) {
    AppleCard {
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Status: ${exam.status} • Target: ${exam.targetYear}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StatusPurpleSubtle
            ) {
                Text(
                    text = "${exam.syllabusProgress}% Syllabus",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusPurple,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Solved ${exam.pyqCompleted}/${exam.totalPyqs} Previous Year Questions",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onAskAi,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Psychology, contentDescription = "AI", modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Ask AI: Balance ${exam.name} with CGPA", fontSize = 12.sp)
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
        title = { Text("Add Internship Application") },
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
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
