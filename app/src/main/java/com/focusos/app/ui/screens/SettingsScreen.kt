package com.focusos.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.focusos.app.data.models.DegreeInfo
import com.focusos.app.data.models.DegreeType
import com.focusos.app.data.models.UserProfile
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*
import com.focusos.app.util.AppUpdateManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: FocusOsRepository,
    onReplayOnboarding: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by repository.userProfile.collectAsState()
    val appSettings by repository.appSettings.collectAsState()
    val degrees by repository.degrees.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditGoalsDialog by remember { mutableStateOf(false) }
    var showEditRoutineDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var isCheckingUpdate by remember { mutableStateOf(false) }

    GlassBackgroundBox {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassDarkTextPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Settings & Profile",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = GlassDarkTextPrimary
                    )
                }
            }

            // 1. USER PROFILE & IDENTITY CARD
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(AccentBlue, AccentCyan)))
                                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.name.take(1).uppercase().ifBlank { "N" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.name.ifBlank { "NOVA User" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassDarkTextPrimary
                            )
                            Text(
                                text = userProfile.email.ifBlank { "Private Account" },
                                fontSize = 12.sp,
                                color = GlassDarkTextSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusBlueSubtle)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = userProfile.userType.ifBlank { "Personal OS" },
                                    fontSize = 11.sp,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Profile", tint = AccentCyan)
                        }
                    }
                }
            }

            // 2. PERSONAL CONTEXT & PRIORITIES
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERSONAL CONTEXT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = AccentCyan
                        )
                        TextButton(onClick = { showEditGoalsDialog = true }) {
                            Text("Edit Goals", fontSize = 12.sp, color = AccentCyan)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("Active Focus Priorities:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                    Spacer(Modifier.height(6.dp))
                    if (userProfile.priorities.isEmpty()) {
                        Text("No specific priorities set", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(userProfile.priorities) { pri ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x18FFFFFF))
                                        .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(pri, fontSize = 11.sp, color = GlassDarkTextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Major Goals:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                    Spacer(Modifier.height(4.dp))
                    if (userProfile.goals.isEmpty()) {
                        Text("• " + userProfile.majorGoal1, fontSize = 12.sp, color = GlassDarkTextSecondary)
                        Text("• " + userProfile.majorGoal2, fontSize = 12.sp, color = GlassDarkTextSecondary)
                    } else {
                        userProfile.goals.forEach { g ->
                            Text("• $g", fontSize = 12.sp, color = GlassDarkTextSecondary)
                        }
                    }
                }
            }

            // 3. DAILY ROUTINE & HABITS
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY ROUTINE & STYLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = AccentPurple
                        )
                        TextButton(onClick = { showEditRoutineDialog = true }) {
                            Text("Edit Routine", fontSize = 12.sp, color = AccentPurple)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Wake: ${userProfile.wakeUpTime.ifBlank { "07:00 AM" }} • Sleep: ${userProfile.sleepTime.ifBlank { "11:00 PM" }}", fontSize = 13.sp, color = GlassDarkTextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Study/Work: ${userProfile.preferredStudyHours.ifBlank { "Evenings" }}", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("Workout: ${userProfile.gymPreference.ifBlank { "Regular sessions" }}", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("Reminder Style: ${userProfile.reminderStyle.ifBlank { "Balanced" }}", fontSize = 12.sp, color = StatusTeal)
                }
            }

            // 4. ACADEMICS & DEGREES
            item {
                GlassCard {
                    Text(
                        text = "ACADEMIC STRUCTURE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = StatusTeal
                    )
                    Spacer(Modifier.height(10.dp))

                    if (degrees.isEmpty()) {
                        Text("No education programs added.", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    } else {
                        degrees.forEach { deg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(deg.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                                    Text("Year ${deg.currentYear} • Current: ${deg.currentCgpa} CGPA (Target: ${deg.targetCgpa})", fontSize = 11.sp, color = GlassDarkTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // 5. NOTIFICATION PREFERENCES
            item {
                GlassCard {
                    Text(
                        text = "NOTIFICATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AccentCyan
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Notifications", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GlassDarkTextPrimary)
                            Text("Smart, non-intrusive reminders", fontSize = 12.sp, color = GlassDarkTextSecondary)
                        }
                        Switch(
                            checked = appSettings.notificationsEnabled,
                            onCheckedChange = {
                                repository.updateSettings(appSettings.copy(notificationsEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentBlue,
                                uncheckedTrackColor = Color(0x20FFFFFF),
                                uncheckedThumbColor = GlassDarkTextSecondary
                            )
                        )
                    }
                }
            }

            // 6. IN-APP UPDATER CARD
            item {
                GlassCard {
                    Text(
                        text = "NOVA VERSION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AccentBlue
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("NOVA — Focus. Progress. Become.", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GlassDarkTextPrimary)
                    Text("Current Version: v1.0 • Multi-User Production Build", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isCheckingUpdate = true
                            coroutineScope.launch {
                                val result = AppUpdateManager.checkForUpdates()
                                isCheckingUpdate = false
                                if (result.isSuccess) {
                                    val update = result.getOrNull()
                                    if (update != null && update.hasUpdate) {
                                        Toast.makeText(context, "New version v" + update.latestVersion + " available!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "NOVA is up to date!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Checked update: Up to date.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.8f))
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Checking...")
                        } else {
                            Text("Check for Updates")
                        }
                    }
                }
            }

            // 7. DATA, PRIVACY & ACCOUNT ACTIONS
            item {
                GlassCard {
                    Text(
                        text = "DATA & PRIVACY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = GlassDarkTextSecondary
                    )
                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x12FFFFFF))
                            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(10.dp))
                            .clickable {
                                Toast.makeText(context, "Exporting personal OS data as JSON...", Toast.LENGTH_LONG).show()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = "Export", tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export All Personal Data (JSON)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x12FFFFFF))
                            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(10.dp))
                            .clickable(onClick = onReplayOnboarding)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Replay", tint = AccentPurple, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Replay Onboarding Guide", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Sign Out Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x20FF453A))
                            .border(1.dp, StatusOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                repository.signOut()
                                Toast.makeText(context, "Signed out of NOVA", Toast.LENGTH_SHORT).show()
                                onSignOut()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = StatusOrange, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign Out", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusOrange)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Delete Account Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x10FF453A))
                            .clickable { showDeleteConfirmDialog = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Delete Account & Personal Data", fontSize = 12.sp, color = StatusOrange.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // EDIT PROFILE DIALOG
        if (showEditProfileDialog) {
            var editName by remember { mutableStateOf(userProfile.name) }
            var editUserType by remember { mutableStateOf(userProfile.userType) }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                containerColor = GlassDarkCard,
                title = { Text("Edit Profile", color = GlassDarkTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Your Name", color = GlassDarkTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassDarkTextPrimary,
                                unfocusedTextColor = GlassDarkTextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = editUserType,
                            onValueChange = { editUserType = it },
                            label = { Text("What describes you", color = GlassDarkTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassDarkTextPrimary,
                                unfocusedTextColor = GlassDarkTextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            repository.updateProfile(userProfile.copy(name = editName.trim(), userType = editUserType.trim()))
                            showEditProfileDialog = false
                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel", color = GlassDarkTextSecondary)
                    }
                }
            )
        }

        // EDIT GOALS DIALOG
        if (showEditGoalsDialog) {
            var goalsText by remember { mutableStateOf(userProfile.goals.joinToString("\n")) }

            AlertDialog(
                onDismissRequest = { showEditGoalsDialog = false },
                containerColor = GlassDarkCard,
                title = { Text("Edit Goals", color = GlassDarkTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter one goal per line:", fontSize = 12.sp, color = GlassDarkTextSecondary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = goalsText,
                            onValueChange = { goalsText = it },
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassDarkTextPrimary,
                                unfocusedTextColor = GlassDarkTextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newGoals = goalsText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            repository.updateProfile(userProfile.copy(goals = newGoals))
                            showEditGoalsDialog = false
                            Toast.makeText(context, "Goals updated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditGoalsDialog = false }) {
                        Text("Cancel", color = GlassDarkTextSecondary)
                    }
                }
            )
        }

        // EDIT ROUTINE DIALOG
        if (showEditRoutineDialog) {
            var wake by remember { mutableStateOf(userProfile.wakeUpTime) }
            var sleep by remember { mutableStateOf(userProfile.sleepTime) }
            var study by remember { mutableStateOf(userProfile.preferredStudyHours) }

            AlertDialog(
                onDismissRequest = { showEditRoutineDialog = false },
                containerColor = GlassDarkCard,
                title = { Text("Edit Routine", color = GlassDarkTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = wake,
                            onValueChange = { wake = it },
                            label = { Text("Wake-up Time", color = GlassDarkTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassDarkTextPrimary,
                                unfocusedTextColor = GlassDarkTextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = sleep,
                            onValueChange = { sleep = it },
                            label = { Text("Sleep Time", color = GlassDarkTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassDarkTextPrimary,
                                unfocusedTextColor = GlassDarkTextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = study,
                            onValueChange = { study = it },
                            label = { Text("Study / Work Hours", color = GlassDarkTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GlassDarkTextPrimary,
                                unfocusedTextColor = GlassDarkTextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            repository.updateProfile(userProfile.copy(wakeUpTime = wake.trim(), sleepTime = sleep.trim(), preferredStudyHours = study.trim()))
                            showEditRoutineDialog = false
                            Toast.makeText(context, "Routine updated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditRoutineDialog = false }) {
                        Text("Cancel", color = GlassDarkTextSecondary)
                    }
                }
            )
        }

        // DELETE ACCOUNT DIALOG
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = GlassDarkCard,
                title = { Text("Delete Account?", color = StatusOrange, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to permanently delete your NOVA account and all associated local and cloud data? This cannot be undone.",
                        fontSize = 13.sp,
                        color = GlassDarkTextPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            repository.signOut()
                            Toast.makeText(context, "Account data wiped and signed out.", Toast.LENGTH_LONG).show()
                            onSignOut()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusOrange)
                    ) {
                        Text("Delete Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = GlassDarkTextSecondary)
                    }
                }
            )
        }
    }
}
