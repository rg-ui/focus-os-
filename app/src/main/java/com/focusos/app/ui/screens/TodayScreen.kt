package com.focusos.app.ui.screens

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import com.focusos.app.ui.components.PriorityBadge
import com.focusos.app.ui.theme.*

@Composable
fun TodayScreen(
    repository: FocusOsRepository,
    onOpenFocusTimer: () -> Unit
) {
    val tasks by repository.tasks.collectAsState()
    val classes by repository.classes.collectAsState()
    val focusSessions by repository.focusSessions.collectAsState()
    val userProfile by repository.userProfile.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(selectedFilter, tasks) {
        when (selectedFilter) {
            "Academics" -> tasks.filter { it.category == TaskCategory.ITEP || it.category == TaskCategory.IITM }
            "Career" -> tasks.filter { it.category == TaskCategory.DATA_SCIENCE || it.category == TaskCategory.INTERNSHIP || it.category == TaskCategory.PROJECT }
            "Health" -> tasks.filter { it.category == TaskCategory.HEALTH }
            else -> tasks
        }
    }

    val todayFocusMinutes = remember(focusSessions) {
        focusSessions.sumOf { it.durationMinutes }
    }

    GlassBackgroundBox {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // 1. HEADER & DATE
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today Timeline",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = GlassDarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Calm execution without overplanning",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassDarkTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusBlueSubtle)
                            .border(1.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { showAddTaskDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Task", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentBlue)
                        }
                    }
                }
            }

            // 2. FOCUS SUMMARY PILL
            item {
                GlassCard(
                    backgroundColor = AccentBlue.copy(alpha = 0.08f),
                    borderGradient = GlassAccentBorderGradient,
                    onClick = onOpenFocusTimer
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StatusBlueSubtle)
                                    .border(1.dp, AccentBlue.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = "Timer", tint = AccentBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (todayFocusMinutes > 0) "${todayFocusMinutes}m Deep Focus Logged" else "Start Deep Focus Timer",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassDarkTextPrimary
                                )
                                Text(
                                    text = "Tap to launch 25m / 50m distraction-free block",
                                    fontSize = 11.sp,
                                    color = GlassDarkTextSecondary
                                )
                            }
                        }

                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = AccentCyan, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // 3. TODAY CLASSES & SESSIONS
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY CLASSES",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = GlassDarkTextSecondary
                        )
                        Text(
                            text = "${classes.count { it.status == ClassAttendanceStatus.PRESENT || it.status == ClassAttendanceStatus.WATCHED_RECORDING }} / ${classes.size} done",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassCard {
                        classes.forEachIndexed { index, session ->
                            ClassSessionRow(
                                session = session,
                                onStatusChange = { newStatus ->
                                    repository.markClassAttendance(session.id, newStatus)
                                }
                            )
                            if (index < classes.size - 1) {
                                HorizontalDivider(
                                    color = Color(0x15FFFFFF),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            // 4. TASK CATEGORY FILTER CHIPS
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("All", "Academics", "Career", "Health")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .background(if (isSelected) AccentBlue.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                                .border(
                                    1.dp,
                                    if (isSelected) AccentBlue.copy(alpha = 0.6f) else Color(0x18FFFFFF),
                                    shape
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AccentCyan else GlassDarkTextSecondary
                            )
                        }
                    }
                }
            }

            // 5. TASKS CHECKLIST
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTION TASKS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = GlassDarkTextSecondary
                        )
                        Text(
                            text = "${filteredTasks.count { it.isCompleted }} / ${filteredTasks.size} Done",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassCard {
                        if (filteredTasks.isEmpty()) {
                            Text(
                                text = "No tasks found in this filter.",
                                fontSize = 13.sp,
                                color = GlassDarkTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            filteredTasks.forEachIndexed { index, task ->
                                TaskRow(
                                    task = task,
                                    onToggle = { repository.toggleTask(task.id) }
                                )
                                if (index < filteredTasks.size - 1) {
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

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { newTask ->
                repository.addTask(newTask)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun ClassSessionRow(
    session: ClassSession,
    onStatusChange: (ClassAttendanceStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.subjectName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GlassDarkTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${session.timeSlot} • ${session.topic}",
                fontSize = 12.sp,
                color = GlassDarkTextSecondary
            )
        }

        Box {
            val statusColor = when (session.status) {
                ClassAttendanceStatus.PRESENT -> StatusGreen
                ClassAttendanceStatus.ABSENT -> StatusRed
                ClassAttendanceStatus.WATCHED_RECORDING -> AccentBlue
                ClassAttendanceStatus.NEED_REVISION -> StatusOrange
                ClassAttendanceStatus.UPCOMING -> GlassDarkTextSecondary
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (session.status) {
                            ClassAttendanceStatus.PRESENT -> "Attended"
                            ClassAttendanceStatus.ABSENT -> "Missed"
                            ClassAttendanceStatus.WATCHED_RECORDING -> "Watched"
                            ClassAttendanceStatus.NEED_REVISION -> "Revise"
                            ClassAttendanceStatus.UPCOMING -> "Upcoming"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.size(16.dp),
                        tint = statusColor
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Mark Present") },
                    onClick = {
                        onStatusChange(ClassAttendanceStatus.PRESENT)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Mark Missed (Auto Catch-up)") },
                    onClick = {
                        onStatusChange(ClassAttendanceStatus.ABSENT)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Watched Recording") },
                    onClick = {
                        onStatusChange(ClassAttendanceStatus.WATCHED_RECORDING)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Needs Revision") },
                    onClick = {
                        onStatusChange(ClassAttendanceStatus.NEED_REVISION)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = "Toggle",
            tint = if (task.isCompleted) StatusGreen else GlassDarkTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (task.isCompleted) GlassDarkTextSecondary else GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = task.category)
                PriorityBadge(priority = task.priority)
                Text(
                    text = "• ${task.estimatedMinutes}m • ${task.deadline}",
                    fontSize = 11.sp,
                    color = GlassDarkTextTertiary
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.IITM) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var estimatedMinutes by remember { mutableStateOf("45") }
    var deadline by remember { mutableStateOf("Today") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task", color = GlassDarkTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", fontSize = 12.sp, color = GlassDarkTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TaskCategory.values()) { category ->
                        val isSel = selectedCategory == category
                        val shape = RoundedCornerShape(8.dp)
                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .background(if (isSel) AccentBlue.copy(alpha = 0.3f) else Color(0x14FFFFFF))
                                .border(1.dp, if (isSel) AccentBlue else Color(0x20FFFFFF), shape)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = category.name.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) AccentCyan else GlassDarkTextSecondary
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = estimatedMinutes,
                        onValueChange = { estimatedMinutes = it },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(
                            TaskItem(
                                id = "task_${System.currentTimeMillis()}",
                                title = title.trim(),
                                category = selectedCategory,
                                priority = selectedPriority,
                                estimatedMinutes = estimatedMinutes.toIntOrNull() ?: 45,
                                deadline = deadline.ifBlank { "Today" }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GlassDarkTextSecondary)
            }
        }
    )
}
