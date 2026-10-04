package com.focusos.app.ui.screens

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
                        text = "Today's Timeline",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Calm execution without overplanning",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StatusBlueSubtle)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", tint = AccentBlue)
                }
            }
        }

        // 2. FOCUS TIMER BANNER
        item {
            AppleCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                borderColor = AccentBlue.copy(alpha = 0.3f),
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(StatusBlueSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = "Timer", tint = AccentBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Focus Sessions",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${todayFocusMinutes}m focused today • 25 / 50 / 90m blocks",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onOpenFocusTimer,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Start", fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. SCHEDULED CLASSES
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLASSES & SESSIONS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${classes.count { it.status == ClassAttendanceStatus.PRESENT }} present",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                classes.forEach { session ->
                    ClassSessionCard(
                        session = session,
                        onStatusChange = { newStatus ->
                            repository.markClassStatus(session.id, newStatus)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // 4. TASKS SECTION WITH FILTER TABS
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TASKS & ACTION ITEMS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${filteredTasks.count { it.isCompleted }}/${filteredTasks.size} done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabs = listOf("All", "Academics", "Career", "Health")
                    items(tabs) { tab ->
                        val isSelected = selectedFilter == tab
                        Surface(
                            onClick = { selectedFilter = tab },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AppleCard {
                    if (filteredTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks in this category",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        filteredTasks.forEachIndexed { index, task ->
                            TaskRow(
                                task = task,
                                onToggle = { repository.toggleTask(task.id) }
                            )
                            if (index < filteredTasks.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. TODAY'S FOCUS HISTORY
        if (focusSessions.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "COMPLETED FOCUS SESSIONS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AppleCard {
                        focusSessions.forEachIndexed { index, session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreenSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = StatusGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.taskTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (session.accomplishmentNotes.isNotBlank()) {
                                        Text(
                                            text = "\"${session.accomplishmentNotes}\"",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "${session.durationMinutes} min",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                            }
                            if (index < focusSessions.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
                                )
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
private fun ClassSessionCard(
    session: ClassSession,
    onStatusChange: (ClassAttendanceStatus) -> Unit
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
                        category = if (session.degreeType == DegreeType.IITM) TaskCategory.IITM else TaskCategory.ITEP
                    )
                    Text(
                        text = session.timeSlot,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session.subjectName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = session.topic,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                Surface(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = when (session.status) {
                        ClassAttendanceStatus.PRESENT -> StatusGreenSubtle
                        ClassAttendanceStatus.ABSENT -> StatusRedSubtle
                        ClassAttendanceStatus.WATCHED_RECORDING -> StatusBlueSubtle
                        ClassAttendanceStatus.NEED_REVISION -> StatusOrangeSubtle
                        ClassAttendanceStatus.UPCOMING -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (session.status) {
                                ClassAttendanceStatus.PRESENT -> "Present"
                                ClassAttendanceStatus.ABSENT -> "Missed"
                                ClassAttendanceStatus.WATCHED_RECORDING -> "Recorded"
                                ClassAttendanceStatus.NEED_REVISION -> "Revise"
                                ClassAttendanceStatus.UPCOMING -> "Upcoming"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (session.status) {
                                ClassAttendanceStatus.PRESENT -> StatusGreen
                                ClassAttendanceStatus.ABSENT -> StatusRed
                                ClassAttendanceStatus.WATCHED_RECORDING -> AccentBlue
                                ClassAttendanceStatus.NEED_REVISION -> StatusOrange
                                ClassAttendanceStatus.UPCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun TaskRow(
    task: TaskItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = "Toggle",
            tint = if (task.isCompleted) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        title = { Text("Create New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TaskCategory.values()) { category ->
                        val isSel = selectedCategory == category
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AccentBlue else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = category.name.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                }
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
