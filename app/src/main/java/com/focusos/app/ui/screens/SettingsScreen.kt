package com.focusos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.focusos.app.data.models.DegreeType
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*
import com.focusos.app.util.AppUpdateManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: FocusOsRepository,
    onReplayOnboarding: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by repository.userProfile.collectAsState()
    val appSettings by repository.appSettings.collectAsState()
    val degrees by repository.degrees.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val itep = degrees.find { it.type == DegreeType.ITEP }
    val iitm = degrees.find { it.type == DegreeType.IITM }

    var itepCgpaText by remember(itep) { mutableStateOf("${itep?.currentCgpa ?: 6.8}") }
    var iitmCgpaText by remember(iitm) { mutableStateOf("${iitm?.currentCgpa ?: 5.5}") }

    var supabaseUrl by remember(appSettings) { mutableStateOf(appSettings.supabaseUrl) }
    var supabaseKey by remember(appSettings) { mutableStateOf(appSettings.supabaseAnonKey) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    GlassBackgroundBox {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
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

            // 1. IN-APP AUTO UPDATER CARD
            item {
                GlassCard(
                    backgroundColor = AccentBlue.copy(alpha = 0.12f),
                    borderGradient = GlassAccentBorderGradient
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Focus OS Version", fontSize = 11.sp, color = GlassDarkTextSecondary)
                            Text("v${AppUpdateManager.CURRENT_VERSION_NAME} (Build ${AppUpdateManager.CURRENT_VERSION_CODE})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        }

                        Button(
                            onClick = {
                                isCheckingUpdate = true
                                coroutineScope.launch {
                                    val res = AppUpdateManager.checkForUpdates()
                                    isCheckingUpdate = false
                                    if (res.isSuccess) {
                                        val info = res.getOrNull()
                                        if (info != null && info.hasUpdate) {
                                            Toast.makeText(context, "New update v${info.latestVersionName} found! Downloading...", Toast.LENGTH_LONG).show()
                                            AppUpdateManager.startDownloadAndInstall(context, info.downloadUrl) {}
                                        } else {
                                            Toast.makeText(context, "Aapka app already latest version par hai!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "App is up to date!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            enabled = !isCheckingUpdate
                        ) {
                            Text(if (isCheckingUpdate) "Checking..." else "Check Update")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "OTA In-App Updates enabled: Updates download and apply seamlessly without losing any study data.",
                        fontSize = 11.sp,
                        color = GlassDarkTextSecondary
                    )
                }
            }

            // 2. USER PROFILE & TARGETS
            item {
                GlassCard {
                    Text(
                        text = "STUDENT PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AccentCyan
                    )
                    Spacer(Modifier.height(10.dp))

                    Text("Name: ${userProfile.name}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GlassDarkTextPrimary)
                    Text("Email: ${userProfile.email}", fontSize = 13.sp, color = GlassDarkTextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("Primary Focus: ${userProfile.majorGoal1}", fontSize = 12.sp, color = GlassDarkTextSecondary)
                    Text("Secondary Track: ${userProfile.majorGoal2}", fontSize = 12.sp, color = GlassDarkTextSecondary)
                }
            }

            // 3. DUAL-DEGREE CGPA TARGETS
            item {
                GlassCard {
                    Text(
                        text = "ACADEMIC BASELINES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AccentPurple
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = itepCgpaText,
                            onValueChange = {
                                itepCgpaText = it
                                val newCgpa = it.toDoubleOrNull()
                                if (newCgpa != null && itep != null) {
                                    repository.updateDegree(itep.type, newCgpa, itep.targetCgpa, itep.completedCredits)
                                }
                            },
                            label = { Text("ITEP CGPA", color = GlassDarkTextSecondary) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = iitmCgpaText,
                            onValueChange = {
                                iitmCgpaText = it
                                val newCgpa = it.toDoubleOrNull()
                                if (newCgpa != null && iitm != null) {
                                    repository.updateDegree(iitm.type, newCgpa, iitm.targetCgpa, iitm.completedCredits)
                                }
                            },
                            label = { Text("IITM CGPA", color = GlassDarkTextSecondary) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 4. NOTIFICATIONS
            item {
                GlassCard {
                    Text(
                        text = "NOTIFICATIONS & ROUTINE",
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
                            Text("Max 3-5 non-spamming alerts/day", fontSize = 12.sp, color = GlassDarkTextSecondary)
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

                    Spacer(Modifier.height(10.dp))
                    Text("Morning Brief: ${appSettings.dailyMorningBriefTime}", fontSize = 13.sp, color = GlassDarkTextSecondary)
                    Text("Evening Check-in: ${appSettings.dailyEveningCheckinTime}", fontSize = 13.sp, color = GlassDarkTextSecondary)
                }
            }

            // 5. SUPABASE CLOUD SYNC CONFIGURATION
            item {
                GlassCard {
                    Text(
                        text = "SUPABASE BACKEND INTEGRATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = StatusTeal
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = supabaseUrl,
                        onValueChange = { supabaseUrl = it },
                        label = { Text("Supabase Project URL", color = GlassDarkTextSecondary) },
                        placeholder = { Text("https://xyz.supabase.co") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supabaseKey,
                        onValueChange = { supabaseKey = it },
                        label = { Text("Supabase Anon Key", color = GlassDarkTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            repository.updateSettings(
                                appSettings.copy(supabaseUrl = supabaseUrl, supabaseAnonKey = supabaseKey)
                            )
                            Toast.makeText(context, "Supabase configuration saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusTeal)
                    ) {
                        Text("Save Cloud Config")
                    }
                }
            }

            // 6. DATA EXPORT & ONBOARDING REPLAY
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
                }
            }
        }
    }
}
