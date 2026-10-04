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
import com.focusos.app.ui.components.AppleScoreRing
import com.focusos.app.ui.components.CategoryBadge
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
    onOpenFocusTimer: () -> Unit
) {
    val userProfile by repository.userProfile.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val degrees by repository.degrees.collectAsState()
    val classes by repository.classes.collectAsState()
    val missedRecoveries by repository.missedRecoveries.collectAsState()
    val dsRoadmap by repository.dsRoadmap.collectAsState()
    val internships by repository.internships.collectAsState()
    val morningBrief by repository.morningBrief.collectAsState()

    val top3Tasks = tasks.filter { it.isTop3 }
    val itepDegree = degrees.find { it.type == DegreeType.ITEP }
    val iitmDegree = degrees.find { it.type == DegreeType.IITM }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 5..11 -> "Good morning, ${userProfile.name}."
        in 12..16 -> "Good afternoon, ${userProfile.name}."
        in 17..21 -> "Good evening, ${userProfile.name}."
        else -> "Good night, ${userProfile.name}."
    }

    val dsProgress = remember(dsRoadmap) {
        val totalStages = dsRoadmap.size
        val totalProgress = dsRoadmap.sumOf { it.progressPercent }
        if (totalStages > 0) totalProgress / totalStages else 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. TOP GREETING HEADER
        item {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Focus on what moves your life forward.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. TODAY'S SCORE CARD
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when {
                                userProfile.todayScore >= 80 -> "Strong Momentum"
                                userProfile.todayScore >= 60 -> "On Track Today"
                                else -> "Building Rhythm"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Calculated from study consistency, focus sessions, health & priority tasks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusBlueSubtle
                            ) {
                                Text(
                                    text = "${userProfile.currentStreakDays}d streak",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusGreenSubtle
                            ) {
                                Text(
                                    text = "${String.format("%.1f", userProfile.totalFocusedHoursThisWeek)}h focused this week",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    AppleScoreRing(score = userProfile.todayScore)
                }
            }
        }

        // 3. TOP 3 PRIORITIES
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOP 3 PRIORITIES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Auto-recommended by AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AppleCard {
                    top3Tasks.forEachIndexed { index, task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { repository.toggleTask(task.id) }
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
                                    text = "${index + 1}. ${task.title}",
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
                                    Text(
                                        text = "•  ${task.estimatedMinutes}m  •  ${task.deadline}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (index < top3Tasks.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }

        // 4. TODAY AT A GLANCE (Classes, Pending Tasks, Missed Alerts)
        item {
            Column {
                Text(
                    text = "TODAY AT A GLANCE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                val unrecoveredMissed = missedRecoveries.count { !it.isRecovered }
                if (unrecoveredMissed > 0) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StatusOrangeSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusOrange.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAcademics() }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "You are $unrecoveredMissed sessions behind. Let's recover them gradually.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppleCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToToday() },
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = "Classes", tint = StatusPurple, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Classes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${classes.size} Scheduled", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${classes.count { it.status == ClassAttendanceStatus.PRESENT || it.status == ClassAttendanceStatus.WATCHED_RECORDING }} completed", fontSize = 11.sp, color = StatusGreen)
                    }

                    AppleCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenFocusTimer() },
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = "Focus", tint = AccentBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Focus Timer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("25 / 50 / 90m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Start deep session", fontSize = 11.sp, color = AccentBlue)
                    }
                }
            }
        }

        // 5. YOUR PROGRESS (Dual Degree, DS Roadmap, Internships)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR PROGRESS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue,
                        modifier = Modifier.clickable { onNavigateToAcademics() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ITEP Card
                    AppleCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToAcademics() },
                        contentPadding = 14.dp
                    ) {
                        Text("ITEP Math", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${itepDegree?.currentCgpa ?: 6.8}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(" / 10", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("Target: ${itepDegree?.targetCgpa ?: 7.5}", fontSize = 11.sp, color = StatusGreen)
                    }

                    // IITM Card
                    AppleCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToAcademics() },
                        contentPadding = 14.dp
                    ) {
                        Text("IITM DS", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${iitmDegree?.currentCgpa ?: 5.5}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(" / 10", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("Target: ${iitmDegree?.targetCgpa ?: 6.5}", fontSize = 11.sp, color = AccentBlue)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Data Science Card
                    AppleCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToCareer() },
                        contentPadding = 14.dp
                    ) {
                        Text("Data Science", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("$dsProgress%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        Spacer(Modifier.height(2.dp))
                        Text("Stage 3 (Pandas)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Internship Card
                    AppleCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToCareer() },
                        contentPadding = 14.dp
                    ) {
                        Text("Internship Hunt", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("${internships.size} Apps", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StatusTeal)
                        Spacer(Modifier.height(2.dp))
                        Text("Target: ₹5k-6k/mo", fontSize = 11.sp, color = StatusGreen)
                    }
                }
            }
        }

        // 6. AI MENTOR INSIGHT CALLOUT
        item {
            AppleCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                borderColor = AccentBlue.copy(alpha = 0.3f),
                onClick = onNavigateToAi
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StatusBlueSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = "AI", tint = AccentBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI MENTOR INSIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"You're trying to work on too many things this week. Finish your IITM Statistics assignment before adding another project.\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to talk to your AI Mentor →",
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
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ActionChip(icon = Icons.Default.School, label = "Academics", color = StatusPurple, onClick = onNavigateToAcademics)
                    }
                    item {
                        ActionChip(icon = Icons.Default.Work, label = "Career & Projects", color = StatusTeal, onClick = onNavigateToCareer)
                    }
                    item {
                        ActionChip(icon = Icons.Default.FitnessCenter, label = "Health & Gym", color = StatusGreen, onClick = onNavigateToHealth)
                    }
                    item {
                        ActionChip(icon = Icons.Default.Timer, label = "Focus Timer", color = AccentBlue, onClick = onOpenFocusTimer)
                    }
                    item {
                        ActionChip(icon = Icons.Default.Psychology, label = "AI Mentor", color = AccentBlue, onClick = onNavigateToAi)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
