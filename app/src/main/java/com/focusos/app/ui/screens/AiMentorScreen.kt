package com.focusos.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.models.*
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.AppleCard
import com.focusos.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiMentorScreen(
    repository: FocusOsRepository,
    initialPrompt: String? = null
) {
    val context = LocalContext.current
    val chatMessages by repository.chatMessages.collectAsState()
    val morningBrief by repository.morningBrief.collectAsState()
    val degrees by repository.degrees.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val userProfile by repository.userProfile.collectAsState()

    var inputText by remember { mutableStateOf(initialPrompt ?: "") }
    var showDailyCheckinDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Chat") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val itep = degrees.find { it.type == DegreeType.ITEP }
    val iitm = degrees.find { it.type == DegreeType.IITM }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            repository.sendUserMessage(initialPrompt)
        }
    }

    val readyPrompts = listOf(
        "Bhai aaj kya karu?" to Icons.Outlined.Lightbulb,
        "Kal IITM assignment hai" to Icons.Outlined.School,
        "Meri CGPA kaise improve hogi?" to Icons.Outlined.TrendingUp,
        "Next 2 hours plan" to Icons.Outlined.Timer,
        "Main distracted ho raha hoon" to Icons.Outlined.DoNotDisturb,
        "GATE karu ya internship?" to Icons.Outlined.WorkOutline
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 1. TOP HEADER & SEGMENTED SWITCHER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StatusBlueSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "AI",
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Personal Mentor",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Context-Aware • Honest • Academic First",
                            fontSize = 11.sp,
                            color = StatusGreen
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            repository.clearChatHistory()
                            Toast.makeText(context, "Chat reset", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { showDailyCheckinDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = "Review", modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Check-In", fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Active Context Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ CONTEXT:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "ITEP ${itep?.currentCgpa ?: 6.8} • IITM ${iitm?.currentCgpa ?: 5.5} • Stats Assignment Due Tomorrow • Score ${userProfile.todayScore}/100",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mode Tabs (Chat vs Morning Brief)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Chat" to "💬 Mentorship Chat", "Morning Brief" to "🌅 Morning Brief").forEach { (tabId, label) ->
                    val isSel = selectedTab == tabId
                    Surface(
                        onClick = { selectedTab = tabId },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) MaterialTheme.colorScheme.surface else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. MAIN CONTENT AREA
        if (selectedTab == "Morning Brief") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
            ) {
                item {
                    AppleCard(
                        backgroundColor = StatusBlueSubtle,
                        borderColor = AccentBlue.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = morningBrief.greeting,
                            style = MaterialTheme.typography.titleLarge,
                            color = AccentBlue
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = morningBrief.academicNotice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    AppleCard {
                        Text(
                            text = "TODAY'S 3 KEY PRIORITIES",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        morningBrief.topPriorities.forEachIndexed { idx, prio ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("${idx + 1}. ", fontWeight = FontWeight.Bold, color = AccentBlue)
                                Text(prio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "RECOMMENDED STUDY BLOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(morningBrief.recommendedStudyBlock, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = StatusGreen)

                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "HEALTH REMINDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(morningBrief.healthReminder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            // CHAT MESSAGES
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(chatMessages) { message ->
                    ChatMessageBubble(
                        message = message,
                        onActionClick = { suggestion ->
                            repository.sendUserMessage(suggestion)
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("AI Mentor", message.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // FLOATING BOTTOM BAR WITH SUGGESTIONS & INPUT
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Quick Prompts Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        items(readyPrompts) { (prompt, icon) ->
                            Surface(
                                onClick = { repository.sendUserMessage(prompt) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = AccentBlue)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = prompt,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Input Field & Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask your mentor anything...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp, max = 100.dp),
                            shape = RoundedCornerShape(22.dp),
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedBorderColor = AccentBlue
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val canSend = inputText.isNotBlank()
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    val textToSend = inputText.trim()
                                    inputText = ""
                                    repository.sendUserMessage(textToSend)
                                }
                            },
                            enabled = canSend,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (canSend) AccentBlue else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDailyCheckinDialog) {
        DailyCheckinDialog(
            onDismiss = { showDailyCheckinDialog = false },
            onSubmitReview = { completed, missed, focusedHrs, tomorrowList ->
                showDailyCheckinDialog = false
                repository.sendUserMessage(
                    "Maine daily check-in complete kiya hai:\n" +
                    "• Focused study: $focusedHrs hours\n" +
                    "• Completed: $completed\n" +
                    "• Missed/Pending: $missed\n" +
                    "• Tomorrow's targets: ${tomorrowList.joinToString(", ")}"
                )
            }
        )
    }
}

@Composable
private fun ChatMessageBubble(
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
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(StatusBlueSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Psychology, contentDescription = "AI", tint = AccentBlue, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isAi) 4.dp else 18.dp,
                    bottomEnd = if (isAi) 18.dp else 4.dp
                ),
                color = if (isAi) MaterialTheme.colorScheme.surfaceVariant else AccentBlue,
                border = if (isAi) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = if (isAi) MaterialTheme.colorScheme.onSurface else Color.White
                    )

                    if (isAi) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Copy",
                                fontSize = 10.sp,
                                color = AccentBlue,
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
                        Surface(
                            onClick = { onActionClick(action) },
                            shape = RoundedCornerShape(8.dp),
                            color = StatusBlueSubtle
                        ) {
                            Text(
                                text = "👉 $action",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyCheckinDialog(
    onDismiss: () -> Unit,
    onSubmitReview: (completed: String, missed: String, focusedHours: Double, tomorrow: List<String>) -> Unit
) {
    var completed by remember { mutableStateOf("Completed IITM Bayes theorem assignment draft and went to gym.") }
    var missed by remember { mutableStateOf("Did not finish Real Analysis theorem 3.") }
    var focusedHours by remember { mutableStateOf("3.5") }
    var tomorrowPrio1 by remember { mutableStateOf("Submit IITM Statistics Assignment") }
    var tomorrowPrio2 by remember { mutableStateOf("Revise Real Analysis theorem proofs") }
    var tomorrowPrio3 by remember { mutableStateOf("Pandas data cleaning on mini project") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Evening Check-In", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("1. What did you complete today?", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = completed, onValueChange = { completed = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
                item {
                    Text("2. How many focused study hours?", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = focusedHours, onValueChange = { focusedHours = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
                item {
                    Text("3. What's pending or was missed?", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = missed, onValueChange = { missed = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                }
                item {
                    Text("4. Tomorrow's Top 3 Priorities:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        completed,
                        missed,
                        focusedHours.toDoubleOrNull() ?: 3.0,
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
