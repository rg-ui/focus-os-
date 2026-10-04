package com.focusos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.focusos.app.data.models.FocusSession
import com.focusos.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FocusTimerModal(
    onDismiss: () -> Unit,
    onSessionCompleted: (FocusSession) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(25) }
    var remainingSeconds by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("Deep Study & Core Focus") }
    var accomplishmentNotes by remember { mutableStateOf("") }

    val totalSeconds = remember(selectedMinutes) { selectedMinutes * 60 }
    val progress = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timerProgress"
    )

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            isCompleted = true
        }
    }

    val minutesLeft = remainingSeconds / 60
    val secondsLeft = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutesLeft, secondsLeft)

    Dialog(onDismissRequest = { if (!isRunning) onDismiss() }) {
        val dialogShape = RoundedCornerShape(26.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dialogShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF141928), Color(0xFF0C0F17))
                    )
                )
                .border(1.dp, GlassBorderGradient, dialogShape)
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isCompleted) {
                    Text(
                        text = "DEEP FOCUS TIMER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Duration selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x12FFFFFF))
                            .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(15 to "15m", 25 to "25m", 50 to "50m", 90 to "90m").forEach { (mins, label) ->
                            val selected = selectedMinutes == mins
                            val pillShape = RoundedCornerShape(8.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(pillShape)
                                    .background(if (selected) AccentBlue.copy(alpha = 0.3f) else Color.Transparent)
                                    .border(1.dp, if (selected) AccentBlue.copy(alpha = 0.6f) else Color.Transparent, pillShape)
                                    .clickable {
                                        if (!isRunning) {
                                            selectedMinutes = mins
                                            remainingSeconds = mins * 60
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) AccentCyan else GlassDarkTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Timer Circular Canvas
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(Color(0x0CFFFFFF))
                            .border(1.dp, Color(0x18FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = Color(0x18FFFFFF)
                        val progressBrush = Brush.sweepGradient(listOf(AccentBlue, AccentCyan, AccentPurple, AccentBlue))

                        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                            if (animatedProgress > 0f) {
                                drawArc(
                                    brush = progressBrush,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassDarkTextPrimary
                            )
                            Text(
                                text = if (isRunning) "FOCUSING" else "READY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                color = if (isRunning) StatusGreen else GlassDarkTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                remainingSeconds = selectedMinutes * 60
                            },
                            shape = RoundedCornerShape(12.dp),
                            enabled = remainingSeconds < totalSeconds
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = GlassDarkTextSecondary)
                            Spacer(Modifier.width(4.dp))
                            Text("Reset", color = GlassDarkTextSecondary)
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color(0x20FFFFFF) else AccentBlue
                            )
                        ) {
                            Icon(
                                if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Start",
                                tint = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isRunning) "Pause" else "Start Session",
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onDismiss) {
                        Text("Close", color = GlassDarkTextSecondary)
                    }
                } else {
                    // Accomplishment Reflection
                    Text(
                        text = "Session Complete!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = StatusGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "What did you accomplish during this $selectedMinutes min block?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassDarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = accomplishmentNotes,
                        onValueChange = { accomplishmentNotes = it },
                        placeholder = { Text("e.g., Solved 5 Bayes theorem questions for IITM assignment", color = GlassDarkTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onSessionCompleted(
                                FocusSession(
                                    id = "fs_${System.currentTimeMillis()}",
                                    durationMinutes = selectedMinutes,
                                    taskTitle = taskName,
                                    accomplishmentNotes = accomplishmentNotes.ifBlank { "Deep study session completed." }
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Save & Log Progress")
                    }
                }
            }
        }
    }
}
