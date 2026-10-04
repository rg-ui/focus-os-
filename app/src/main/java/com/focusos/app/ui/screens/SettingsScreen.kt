package com.focusos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.focusos.app.data.models.DegreeType
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.AppleCard
import com.focusos.app.ui.theme.*

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

    val itep = degrees.find { it.type == DegreeType.ITEP }
    val iitm = degrees.find { it.type == DegreeType.IITM }

    var itepCgpaText by remember(itep) { mutableStateOf("${itep?.currentCgpa ?: 6.8}") }
    var iitmCgpaText by remember(iitm) { mutableStateOf("${iitm?.currentCgpa ?: 5.5}") }

    var supabaseUrl by remember(appSettings) { mutableStateOf(appSettings.supabaseUrl) }
    var supabaseKey by remember(appSettings) { mutableStateOf(appSettings.supabaseAnonKey) }

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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Settings & Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 1. PROFILE CARD
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(userProfile.name, style = MaterialTheme.typography.titleLarge)
                        Text(userProfile.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusBlueSubtle
                    ) {
                        Text(
                            "Focus OS v1.0",
                            fontSize = 11.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Core Philosophy: Max 2 active priorities at a time.", fontSize = 12.sp, color = StatusGreen)
            }
        }

        // 2. DEGREE & CGPA CONFIGURATION
        item {
            AppleCard {
                Text(
                    text = "ACADEMIC PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = itepCgpaText,
                    onValueChange = { itepCgpaText = it },
                    label = { Text("ITEP Math Current CGPA") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = iitmCgpaText,
                    onValueChange = { iitmCgpaText = it },
                    label = { Text("IITM Data Science Current CGPA") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val itepVal = itepCgpaText.toDoubleOrNull() ?: 6.8
                        val iitmVal = iitmCgpaText.toDoubleOrNull() ?: 5.5
                        if (itep != null) repository.updateDegree(DegreeType.ITEP, itepVal, itep.targetCgpa, itep.completedCredits)
                        if (iitm != null) repository.updateDegree(DegreeType.IITM, iitmVal, iitm.targetCgpa, iitm.completedCredits)
                        Toast.makeText(context, "CGPA values updated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Academic Updates")
                }
            }
        }

        // 3. NOTIFICATIONS
        item {
            AppleCard {
                Text(
                    text = "NOTIFICATIONS & ROUTINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Enable Notifications", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Max 3-5 non-spamming alerts/day", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = appSettings.notificationsEnabled,
                        onCheckedChange = {
                            repository.updateSettings(appSettings.copy(notificationsEnabled = it))
                        }
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text("Morning Brief: ${appSettings.dailyMorningBriefTime}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Evening Check-in: ${appSettings.dailyEveningCheckinTime}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 4. SUPABASE & CLOUD SYNC CONFIGURATION
        item {
            AppleCard {
                Text(
                    text = "SUPABASE BACKEND INTEGRATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = supabaseUrl,
                    onValueChange = { supabaseUrl = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://xyz.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = supabaseKey,
                    onValueChange = { supabaseKey = it },
                    label = { Text("Supabase Anon Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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

        // 5. DATA EXPORT & ONBOARDING REPLAY
        item {
            AppleCard {
                Text(
                    text = "DATA & PRIVACY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Exporting personal OS data as JSON...", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export")
                    Spacer(Modifier.width(6.dp))
                    Text("Export All Personal Data (JSON)")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onReplayOnboarding,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Replay")
                    Spacer(Modifier.width(6.dp))
                    Text("Replay Onboarding Guide")
                }
            }
        }
    }
}
