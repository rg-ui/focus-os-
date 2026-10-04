package com.focusos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.focusos.app.data.models.FocusSession
import com.focusos.app.data.models.TaskCategory
import com.focusos.app.ui.theme.AccentBlue
import com.focusos.app.ui.theme.StatusGreen
import kotlinx.coroutines.delay

@Composable
fun FocusTimerModal(
    onDismiss: () -> Unit,
    onSessionCompleted: (FocusSession) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(25) }
    var remainingSeconds by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("Deep Academic Study") }
    var showAccomplishmentPrompt by remember { mutableStateOf(false) }
    var accomplishmentNotes by remember { mutableStateOf("") }

    val totalSeconds = selectedMinutes * 60

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            showAccomplishmentPrompt = true
        }
    }

    val progress = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds.toFloat()) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "focusProgress")

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Dialog(onDismissRequest = { if (!isRunning) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!showAccomplishmentPrompt) {
                    Text(
                        text = "Focus Session",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calm, undistracted momentum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mode Selection Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(25 to "25 min", 50 to "50 min", 90 to "90 min").forEach { (mins, label) ->
                            val selected = selectedMinutes == mins
                            Surface(
                                onClick = {
                                    if (!isRunning) {
                                        selectedMinutes = mins
                                        remainingSeconds = mins * 60
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Timer Circular Canvas
                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = AccentBlue,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isRunning) "FOCUSING" else "READY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                color = if (isRunning) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant
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
                            Icon(Icons.Default.Refresh, contentDescription = "Reset")
                            Spacer(Modifier.width(4.dp))
                            Text("Reset")
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) MaterialTheme.colorScheme.surfaceVariant else AccentBlue
                            )
                        ) {
                            Icon(
                                if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Start",
                                tint = if (isRunning) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isRunning) "Pause" else "Start Session",
                                color = if (isRunning) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onDismiss) {
                        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Accomplishment Reflection
                    Text(
                        text = "Session Complete!",
                        style = MaterialTheme.typography.titleLarge,
                        color = StatusGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "What did you accomplish during this $selectedMinutes min block?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = accomplishmentNotes,
                        onValueChange = { accomplishmentNotes = it },
                        placeholder = { Text("e.g., Solved 5 Bayes theorem questions for IITM assignment") },
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
