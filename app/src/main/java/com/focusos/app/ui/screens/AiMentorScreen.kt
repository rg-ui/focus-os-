package com.focusos.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.models.*
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiMentorScreen(
    repository: FocusOsRepository,
    initialPrompt: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatMessages by repository.chatMessages.collectAsState()
    val morningBrief by repository.morningBrief.collectAsState()

    var inputText by remember { mutableStateOf(initialPrompt ?: "") }
    val listState = rememberLazyListState()

    var showDailyCheckinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    GlassBackgroundBox {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HEADER & PROMPT CHIPS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Mentor & Coach",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = GlassDarkTextPrimary
                        )
                        Text(
                            text = "Contextual academic, career & schedule guidance",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassDarkTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x12FFFFFF))
                            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                            .clickable { repository.clearChatHistory() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear", fontSize = 11.sp, color = GlassDarkTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassTriggerButton(
                        icon = Icons.Default.WbSunny,
                        label = "Morning Brief",
                        color = StatusOrange,
                        onClick = {
                            repository.sendUserMessage("Morning brief: Aaj ka daily overview aur priority plan generate karo.")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    GlassTriggerButton(
                        icon = Icons.Default.RateReview,
                        label = "Evening Check-in",
                        color = AccentPurple,
                        onClick = { showDailyCheckinDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. CHAT MESSAGES STREAM
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(chatMessages) { msg ->
                    ChatMessageGlassBubble(
                        message = msg,
                        onActionClick = { actionText ->
                            repository.sendUserMessage(actionText)
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("AI Mentor", msg.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // 3. BOTTOM GLASS INPUT BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x18090B10))
                    .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask your AI Mentor anything...", fontSize = 13.sp, color = GlassDarkTextSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color(0x25FFFFFF),
                            focusedContainerColor = Color(0x12FFFFFF),
                            unfocusedContainerColor = Color(0x12FFFFFF),
                            focusedTextColor = GlassDarkTextPrimary,
                            unfocusedTextColor = GlassDarkTextPrimary
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    val sendShape = CircleShape
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(sendShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AccentBlue, AccentPurple)
                                )
                            )
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    val text = inputText.trim()
                                    inputText = ""
                                    repository.sendUserMessage(text)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDailyCheckinDialog) {
        DailyCheckinGlassDialog(
            onDismiss = { showDailyCheckinDialog = false },
            onSubmitReview = { completed, missed, focusedHrs, tomorrowList ->
                showDailyCheckinDialog = false
                repository.sendUserMessage(
                    "Maine daily check-in kiya hai:\n" +
                    "• Focused study: $focusedHrs hours\n" +
                    "• Completed: $completed\n" +
                    "• Missed/Pending: $missed\n" +
                    "• Tomorrow targets: ${tomorrowList.joinToString(", ")}"
                )
            }
        )
    }
}

@Composable
private fun GlassTriggerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0x12FFFFFF))
            .border(1.dp, Brush.linearGradient(listOf(color.copy(alpha = 0.4f), Color(0x10FFFFFF))), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
        }
    }
}

@Composable
private fun ChatMessageGlassBubble(
    message: ChatMessage,
    onActionClick: (String) -> Unit,
    onCopy: () -> Unit
) {
    val isAi = message.sender == MessageSender.AI

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        if (isAi) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(StatusBlueSubtle)
                    .border(1.dp, AccentBlue.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Psychology, contentDescription = "AI", tint = AccentCyan, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
        ) {
            val bubbleShape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isAi) 4.dp else 18.dp,
                bottomEnd = if (isAi) 18.dp else 4.dp
            )

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(
                        if (isAi)
                            Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x10FFFFFF)))
                        else
                            Brush.linearGradient(listOf(AccentBlue, Color(0xFF2563EB)))
                    )
                    .border(
                        1.dp,
                        if (isAi) GlassBorderGradient else Brush.linearGradient(listOf(Color(0x60FFFFFF), Color(0x10FFFFFF))),
                        bubbleShape
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                        color = if (isAi) GlassDarkTextPrimary else Color.White
                    )

                    if (isAi) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Copy",
                                fontSize = 10.sp,
                                color = AccentCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onCopy() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (message.quickActionSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(message.quickActionSuggestions) { action ->
                        val shape = RoundedCornerShape(8.dp)
                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .background(StatusBlueSubtle)
                                .border(1.dp, AccentBlue.copy(alpha = 0.35f), shape)
                                .clickable { onActionClick(action) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "👉 $action",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
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
fun DailyCheckinGlassDialog(
    onDismiss: () -> Unit,
    onSubmitReview: (completed: String, missed: String, focusedHours: Double, tomorrow: List<String>) -> Unit
) {
    var completed by remember { mutableStateOf("") }
    var missed by remember { mutableStateOf("") }
    var focusedHours by remember { mutableStateOf("2.0") }
    var tomorrowPrio1 by remember { mutableStateOf("") }
    var tomorrowPrio2 by remember { mutableStateOf("") }
    var tomorrowPrio3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Evening Check-In", fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("1. What did you complete today?", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    OutlinedTextField(value = completed, onValueChange = { completed = it }, placeholder = { Text("e.g. Completed Stats assignment", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
                item {
                    Text("2. How many focused study hours?", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    OutlinedTextField(value = focusedHours, onValueChange = { focusedHours = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
                item {
                    Text("3. What was pending or missed?", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    OutlinedTextField(value = missed, onValueChange = { missed = it }, placeholder = { Text("e.g. Real Analysis proofs", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
                item {
                    Text("4. Tomorrow Top Priorities:", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    OutlinedTextField(value = tomorrowPrio1, onValueChange = { tomorrowPrio1 = it }, label = { Text("Priority 1") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = tomorrowPrio2, onValueChange = { tomorrowPrio2 = it }, label = { Text("Priority 2") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = tomorrowPrio3, onValueChange = { tomorrowPrio3 = it }, label = { Text("Priority 3") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitReview(
                        completed.ifBlank { "Logged daily focus" },
                        missed.ifBlank { "None" },
                        focusedHours.toDoubleOrNull() ?: 2.0,
                        listOf(tomorrowPrio1, tomorrowPrio2, tomorrowPrio3).filter { it.isNotBlank() }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Generate AI Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = GlassDarkTextSecondary) }
        }
    )
}
