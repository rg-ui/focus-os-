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
                        text = "Health & Routine",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = GlassDarkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Physical Energy, Meaningful Habits & Mindful Screen Time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassDarkTextSecondary
                    )
                }
            }

            // 2. GYM / WORKOUT SECTION
            item {
                GlassCard(
                    borderGradient = if (healthLog.gymCompleted)
                        Brush.linearGradient(listOf(StatusGreen.copy(alpha = 0.5f), Color(0x15FFFFFF)))
                    else GlassBorderGradient
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GYM & WORKOUT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = AccentCyan
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (healthLog.gymCompleted) "Workout Completed" else "Workout Pending",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (healthLog.gymCompleted) StatusGreen else GlassDarkTextPrimary
                            )
                        }

                        Switch(
                            checked = healthLog.gymCompleted,
                            onCheckedChange = { isChecked ->
                                repository.updateHealthLog(healthLog.copy(gymCompleted = isChecked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusGreen,
                                uncheckedTrackColor = Color(0x20FFFFFF),
                                uncheckedThumbColor = GlassDarkTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Workout type selector
                    Text("Select Routine:", fontSize = 11.sp, color = GlassDarkTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(WorkoutType.values()) { type ->
                            val isSel = healthLog.workoutType == type
                            val shape = RoundedCornerShape(8.dp)
                            Box(
                                modifier = Modifier
                                    .clip(shape)
                                    .background(if (isSel) StatusGreen.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                                    .border(1.dp, if (isSel) StatusGreen.copy(alpha = 0.6f) else Color(0x18FFFFFF), shape)
                                    .clickable {
                                        repository.updateHealthLog(healthLog.copy(workoutType = type))
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = type.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) StatusGreen else GlassDarkTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Duration & Energy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x10FFFFFF))
                                .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Duration", fontSize = 11.sp, color = GlassDarkTextSecondary)
                                Text("${healthLog.workoutDurationMinutes} mins", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x10FFFFFF))
                                .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Energy Boost", fontSize = 11.sp, color = GlassDarkTextSecondary)
                                Text(if (healthLog.gymCompleted) "High (+15 Score)" else "Pending", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (healthLog.gymCompleted) StatusGreen else GlassDarkTextSecondary)
                            }
                        }
                    }
                }
            }

            // 3. DAILY HEALTH METRICS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sleep Metric
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bedtime, contentDescription = "Sleep", tint = StatusIndigo, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sleep", fontSize = 12.sp, color = GlassDarkTextSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (healthLog.sleepHours > 0) "${healthLog.sleepHours}h" else "--",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassDarkTextPrimary
                        )
                        Text(
                            text = if (healthLog.sleepHours >= 7.0) "Target met (7h+)" else "Log sleep hours",
                            fontSize = 11.sp,
                            color = if (healthLog.sleepHours >= 7.0) StatusGreen else GlassDarkTextTertiary
                        )
                    }

                    // Water Metric
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = "Water", tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Hydration", fontSize = 12.sp, color = GlassDarkTextSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${healthLog.waterGlasses} Glasses",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassDarkTextPrimary
                        )
                        Text("Target: 8 glasses", fontSize = 11.sp, color = GlassDarkTextSecondary)
                    }
                }
            }

            // 4. HABITS TRACKER (GLASS GRID)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HABIT STREAKS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = GlassDarkTextSecondary
                        )
                        val doneHabits = habits.count { it.completedToday }
                        Text(
                            text = "$doneHabits/${habits.size} done today",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassCard {
                        habits.forEachIndexed { index, habit ->
                            HabitGlassRow(
                                habit = habit,
                                onToggle = { repository.toggleHabit(habit.id) }
                            )
                            if (index < habits.size - 1) {
                                HorizontalDivider(
                                    color = Color(0x15FFFFFF),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            // 5. SCREEN TIME & DISTRACTION CONTROL
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SCREEN TIME & DISTRACTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = StatusOrange
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${distractionLog.todayTotalMinutes} mins total",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = GlassDarkTextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (distractionLog.todayTotalMinutes <= 90) StatusGreenSubtle else StatusOrangeSubtle)
                                .border(1.dp, if (distractionLog.todayTotalMinutes <= 90) StatusGreen.copy(alpha = 0.4f) else StatusOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (distractionLog.todayTotalMinutes <= 90) "Healthy" else "Caution",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (distractionLog.todayTotalMinutes <= 90) StatusGreen else StatusOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Breakdown Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DistractionGlassMetricBox(
                            label = "Instagram",
                            minutes = distractionLog.instagramMinutes,
                            onIncrement = { repository.updateDistractionLog(distractionLog.instagramMinutes + 15, distractionLog.youtubeMinutes, distractionLog.gamingMinutes, distractionLog.otherMinutes) },
                            modifier = Modifier.weight(1f)
                        )
                        DistractionGlassMetricBox(
                            label = "YouTube",
                            minutes = distractionLog.youtubeMinutes,
                            onIncrement = { repository.updateDistractionLog(distractionLog.instagramMinutes, distractionLog.youtubeMinutes + 15, distractionLog.gamingMinutes, distractionLog.otherMinutes) },
                            modifier = Modifier.weight(1f)
                        )
                        DistractionGlassMetricBox(
                            label = "Other",
                            minutes = distractionLog.otherMinutes,
                            onIncrement = { repository.updateDistractionLog(distractionLog.instagramMinutes, distractionLog.youtubeMinutes, 0, distractionLog.otherMinutes + 15) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 6. DAILY JOURNAL (GLASS)
            item {
                GlassCard {
                    Text(
                        text = "DAILY EVENING JOURNAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AccentCyan
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = whatWentWell,
                        onValueChange = {
                            whatWentWell = it
                            repository.updateJournal(journal.copy(whatWentWell = it))
                        },
                        label = { Text("What went well today?", color = GlassDarkTextSecondary) },
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
                        label = { Text("What went wrong?", color = GlassDarkTextSecondary) },
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
                        label = { Text("What will I improve tomorrow?", color = GlassDarkTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (journal.aiPatternInsight.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusBlueSubtle)
                                .border(1.dp, AccentBlue.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 AI Insight: ${journal.aiPatternInsight}",
                                fontSize = 12.sp,
                                color = AccentCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitGlassRow(
    habit: HabitItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${habit.currentStreak} day streak",
                fontSize = 11.sp,
                color = if (habit.currentStreak > 0) StatusGreen else GlassDarkTextTertiary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (habit.completedToday) StatusGreenSubtle else Color(0x12FFFFFF))
                .border(1.dp, if (habit.completedToday) StatusGreen.copy(alpha = 0.5f) else Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (habit.completedToday) "Done ✓" else "Check in",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (habit.completedToday) StatusGreen else GlassDarkTextSecondary
            )
        }
    }
}

@Composable
private fun DistractionGlassMetricBox(
    label: String,
    minutes: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0x12FFFFFF))
            .border(1.dp, Color(0x18FFFFFF), shape)
            .clickable(onClick = onIncrement)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 11.sp, color = GlassDarkTextSecondary)
            Spacer(Modifier.height(2.dp))
            Text("${minutes}m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
            Text("+15m", fontSize = 9.sp, color = AccentCyan)
        }
    }
}
