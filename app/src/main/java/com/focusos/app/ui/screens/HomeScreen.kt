package com.focusos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.focusos.app.ui.components.AppleScoreRing
import com.focusos.app.ui.components.CategoryBadge
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    repository: FocusOsRepository,
    onNavigateToToday: () -> Unit,
    onNavigateToAcademics: () -> Unit,
    onNavigateToCareer: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToAi: () -> Unit,
    onOpenFocusTimer: () -> Unit,
    onAddTask: () -> Unit
) {
    val userProfile by repository.userProfile.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val degrees by repository.degrees.collectAsState()
    val classes by repository.classes.collectAsState()
    val missedRecoveries by repository.missedRecoveries.collectAsState()
    val dsRoadmap by repository.dsRoadmap.collectAsState()
    val internships by repository.internships.collectAsState()
    val focusSessions by repository.focusSessions.collectAsState()
    val healthLog by repository.healthLog.collectAsState()

    val top3Tasks = tasks.filter { it.isTop3 }
    val displayTasks = if (top3Tasks.isNotEmpty()) top3Tasks else tasks.take(3)

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val userName = userProfile.name.ifBlank { "there" }
    val greeting = when (currentHour) {
        in 5..11 -> "Good morning, $userName."
        in 12..16 -> "Good afternoon, $userName."
        in 17..21 -> "Good evening, $userName."
        else -> "Good night, $userName."
    }

    val dsProgress = remember(dsRoadmap) {
        val totalStages = dsRoadmap.size
        val totalProgress = dsRoadmap.sumOf { it.progressPercent }
        if (totalStages > 0) totalProgress / totalStages else 0
    }

    val totalCompletedTasks = remember(tasks) { tasks.count { it.isCompleted } }

    GlassBackgroundBox {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // 1. TOP GREETING & STATUS
            item {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = GlassDarkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Focus on what moves your life forward.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassDarkTextSecondary
                    )
                }
            }

            // 2. TODAY'S SCORE CARD
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TODAY'''S MOMENTUM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when {
                                    userProfile.todayScore >= 80 -> "Peak Focus"
                                    userProfile.todayScore >= 50 -> "Active Momentum"
                                    userProfile.todayScore > 0 -> "In Motion"
                                    else -> "Ready to Begin"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = GlassDarkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (userProfile.todayScore == 0)
                                    "Complete your first task and log focus to build today'''s score."
                                else
                                    "Calculated from completed tasks, deep focus & healthy habits.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassDarkTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StatusBlueSubtle)
                                        .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${userProfile.currentStreakDays}d streak",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentBlue
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StatusGreenSubtle)
                                        .border(1.dp, StatusGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", userProfile.totalFocusedHoursThisWeek)}h focused",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StatusGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        AppleScoreRing(score = userProfile.todayScore)
                    }
                }
            }

            // 3. TOP PRIORITIES & TASKS
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOP PRIORITIES",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = GlassDarkTextSecondary
                        )
                        Text(
                            text = if (tasks.isNotEmpty()) "$totalCompletedTasks/${tasks.size} done" else "+ Add Task",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AccentCyan,
                            modifier = Modifier.clickable { onAddTask() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassCard {
                        if (displayTasks.isEmpty()) {
                            // Beautiful Empty State
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Your day is clear. Let'''s decide what matters.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassDarkTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add 1-3 high priority tasks to protect your focus today.",
                                    fontSize = 12.sp,
                                    color = GlassDarkTextSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(StatusBlueSubtle)
                                        .border(1.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable(onClick = onAddTask)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("+ Add First Priority Task", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentCyan)
                                }
                            }
                        } else {
                            displayTasks.forEachIndexed { index, task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { repository.toggleTask(task.id) }
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
                                            text = "${index + 1}. ${task.title}",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (task.isCompleted) GlassDarkTextSecondary else GlassDarkTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CategoryBadge(category = task.category)
                                            Text(
                                                text = "•  ${task.estimatedMinutes}m  •  ${task.deadline}",
                                                fontSize = 11.sp,
                                                color = GlassDarkTextTertiary
                                            )
                                        }
                                    }
                                }
                                if (index < displayTasks.size - 1) {
                                    HorizontalDivider(
                                        color = Color(0x18FFFFFF),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. TODAY AT A GLANCE (Deep Focus + Classes / Routine)
            item {
                Column {
                    Text(
                        text = "TODAY AT A GLANCE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = GlassDarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val unrecoveredMissed = missedRecoveries.count { !it.isRecovered }
                    if (unrecoveredMissed > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(StatusOrangeSubtle)
                                .border(1.dp, StatusOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { onNavigateToAcademics() }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Alert",
                                    tint = StatusOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$unrecoveredMissed class session needs attention",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = GlassDarkTextPrimary
                                    )
                                    Text(
                                        text = "Catch up on notes & recording to keep your CGPA safe.",
                                        fontSize = 12.sp,
                                        color = GlassDarkTextSecondary
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = GlassDarkTextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Deep Focus Card
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenFocusTimer() },
                            contentPadding = 14.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = "Focus", tint = AccentCyan, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Deep Focus", fontSize = 12.sp, color = GlassDarkTextSecondary)
                            }
                            Spacer(Modifier.height(8.dp))
                            val totalFocusMins = focusSessions.sumOf { it.durationMinutes }
                            Text("${totalFocusMins}m Logged", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                            Text("Start timer →", fontSize = 11.sp, color = AccentCyan)
                        }

                        // Workout / Health Card
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToHealth() },
                            contentPadding = 14.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = "Health", tint = StatusGreen, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Energy & Gym", fontSize = 12.sp, color = GlassDarkTextSecondary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (healthLog.gymCompleted) "Completed ✓" else "Pending",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (healthLog.gymCompleted) StatusGreen else GlassDarkTextPrimary
                            )
                            Text("View routine →", fontSize = 11.sp, color = StatusGreen)
                        }
                    }
                }
            }

            // 5. CORE ACADEMIC & CAREER TRACKS
            if (degrees.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "ACADEMIC BASELINES",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = GlassDarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            degrees.take(2).forEach { deg ->
                                GlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToAcademics() },
                                    contentPadding = 14.dp
                                ) {
                                    Text(deg.name, fontSize = 12.sp, color = GlassDarkTextSecondary, maxLines = 1)
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("${deg.currentCgpa}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                                        Text(" / 10", fontSize = 12.sp, color = GlassDarkTextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text("Target: ${deg.targetCgpa}", fontSize = 11.sp, color = AccentCyan)
                                }
                            }
                        }
                    }
                }
            }

            // 6. AI COACH INSIGHT
            item {
                GlassCard(
                    backgroundColor = AccentBlue.copy(alpha = 0.12f),
                    borderGradient = GlassAccentBorderGradient,
                    onClick = onNavigateToAi
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(StatusBlueSubtle)
                                .border(1.dp, AccentCyan.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = "AI", tint = AccentCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NOVA AI COACH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Focus on doing 1-2 core things with deep clarity rather than scattering your energy today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassDarkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ask your AI Coach anything →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentBlue
                            )
                        }
                    }
                }
            }

            // 7. FAST ACTION CHIPS
            item {
                Column {
                    Text(
                        text = "QUICK NAVIGATION",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = GlassDarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            GlassActionChip(icon = Icons.Default.School, label = "Academics", color = AccentPurple, onClick = onNavigateToAcademics)
                        }
                        item {
                            GlassActionChip(icon = Icons.Default.Work, label = "Career & Skills", color = StatusTeal, onClick = onNavigateToCareer)
                        }
                        item {
                            GlassActionChip(icon = Icons.Default.FitnessCenter, label = "Health & Gym", color = StatusGreen, onClick = onNavigateToHealth)
                        }
                        item {
                            GlassActionChip(icon = Icons.Default.Timer, label = "Focus Timer", color = AccentCyan, onClick = onOpenFocusTimer)
                        }
                        item {
                            GlassActionChip(icon = Icons.Default.Psychology, label = "AI Coach", color = AccentBlue, onClick = onNavigateToAi)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(Color(0x14FFFFFF))
            .border(1.dp, Brush.linearGradient(listOf(color.copy(alpha = 0.4f), Color(0x10FFFFFF))), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GlassDarkTextPrimary)
        }
    }
}
