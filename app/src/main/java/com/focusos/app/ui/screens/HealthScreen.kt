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
fun HealthScreen(
    repository: FocusOsRepository
) {
    val healthLog by repository.healthLog.collectAsState()
    val habits by repository.habits.collectAsState()
    val distractionLog by repository.distractionLog.collectAsState()
    val journal by repository.journal.collectAsState()

    var whatWentWell by remember(journal) { mutableStateOf(journal.whatWentWell) }
    var whatWentWrong by remember(journal) { mutableStateOf(journal.whatWentWrong) }
    var whatToImprove by remember(journal) { mutableStateOf(journal.whatToImproveTomorrow) }

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
                    text = "Health & Routine",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Physical Energy, Meaningful Habits & Mindful Screen Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. GYM / WORKOUT SECTION
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GYM & WORKOUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (healthLog.gymCompleted) "Workout Completed" else "Workout Pending",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (healthLog.gymCompleted) StatusGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = healthLog.gymCompleted,
                        onCheckedChange = { isChecked ->
                            repository.updateHealthLog(healthLog.copy(gymCompleted = isChecked))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StatusGreen)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Workout type selector
                Text("Select Routine:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WorkoutType.values()) { type ->
                        val isSel = healthLog.workoutType == type
                        Surface(
                            onClick = {
                                repository.updateHealthLog(healthLog.copy(workoutType = type))
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) StatusGreen else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = type.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Duration: ${healthLog.workoutDurationMinutes} mins • 4 days completed this week",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. KEY VITALS METRICS GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppleCard(modifier = Modifier.weight(1f), contentPadding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = "Steps", tint = StatusTeal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Steps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${healthLog.steps}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Goal: 7,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                AppleCard(modifier = Modifier.weight(1f), contentPadding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bedtime, contentDescription = "Sleep", tint = StatusPurple, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sleep", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${healthLog.sleepHours} hrs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Quality: Good", fontSize = 10.sp, color = StatusGreen)
                }

                AppleCard(modifier = Modifier.weight(1f), contentPadding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = "Water", tint = AccentBlue, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Water", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${healthLog.waterGlasses} gls", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Goal: 8+", fontSize = 10.sp, color = StatusGreen)
                }
            }
        }

        // 4. MEANINGFUL HABITS (Max 7)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORE HABITS (MAX 7)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${habits.count { it.completedToday }}/${habits.size} done today",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AppleCard {
                    habits.forEachIndexed { index, habit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { repository.toggleHabit(habit.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = habit.completedToday,
                                onCheckedChange = { repository.toggleHabit(habit.id) },
                                colors = CheckboxDefaults.colors(checkedColor = StatusGreen)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = habit.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (habit.completedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${habit.currentStreak} day streak",
                                    fontSize = 11.sp,
                                    color = AccentBlue
                                )
                            }

                            // 7-day consistency dots
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                habit.last7Days.forEach { isDone ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) StatusGreen else MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                        if (index < habits.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }

        // 5. DISTRACTION & SCREEN TIME TRACKER
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "MINDFUL SCREEN TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        val hrs = distractionLog.todayTotalMinutes / 60
                        val mins = distractionLog.todayTotalMinutes % 60
                        Text(
                            text = "Today: ${hrs}h ${mins}m",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (distractionLog.isImprovement) StatusGreenSubtle else StatusOrangeSubtle
                    ) {
                        Text(
                            text = if (distractionLog.isImprovement)
                                "Reduced by ${distractionLog.diffMinutes}m"
                            else
                                "+${-distractionLog.diffMinutes}m vs yesterday",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (distractionLog.isImprovement) StatusGreen else StatusOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DistractionMetricBox(
                        label = "Instagram",
                        minutes = distractionLog.instagramMinutes,
                        onUpdate = { repository.updateDistractionLog(it, distractionLog.youtubeMinutes, distractionLog.gamingMinutes, distractionLog.otherMinutes) },
                        modifier = Modifier.weight(1f)
                    )
                    DistractionMetricBox(
                        label = "YouTube",
                        minutes = distractionLog.youtubeMinutes,
                        onUpdate = { repository.updateDistractionLog(distractionLog.instagramMinutes, it, distractionLog.gamingMinutes, distractionLog.otherMinutes) },
                        modifier = Modifier.weight(1f)
                    )
                    DistractionMetricBox(
                        label = "Gaming/Other",
                        minutes = distractionLog.gamingMinutes + distractionLog.otherMinutes,
                        onUpdate = { repository.updateDistractionLog(distractionLog.instagramMinutes, distractionLog.youtubeMinutes, 0, it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. DAILY JOURNAL
        item {
            AppleCard {
                Text(
                    text = "DAILY EVENING JOURNAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = whatWentWell,
                    onValueChange = {
                        whatWentWell = it
                        repository.updateJournal(journal.copy(whatWentWell = it))
                    },
                    label = { Text("What went well today?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatWentWrong,
                    onValueChange = {
                        whatWentWrong = it
                        repository.updateJournal(journal.copy(whatWentWrong = it))
                    },
                    label = { Text("What went wrong?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatToImprove,
                    onValueChange = {
                        whatToImprove = it
                        repository.updateJournal(journal.copy(whatToImproveTomorrow = it))
                    },
                    label = { Text("What will I improve tomorrow?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (journal.aiPatternInsight.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StatusBlueSubtle
                    ) {
                        Text(
                            text = "💡 AI Insight: ${journal.aiPatternInsight}",
                            fontSize = 12.sp,
                            color = AccentBlue,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DistractionMetricBox(
    label: String,
    minutes: Int,
    onUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text("${minutes}m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
