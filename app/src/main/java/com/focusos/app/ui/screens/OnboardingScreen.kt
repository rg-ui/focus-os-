package com.focusos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    GlassBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Step Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..6).forEach { i ->
                    val isPastOrCurrent = i <= step
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPastOrCurrent) AccentCyan else Color(0x20FFFFFF))
                    )
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(StatusBlueSubtle)
                                    .border(1.dp, AccentBlue.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎯", fontSize = 38.sp)
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Welcome to Focus OS",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = GlassDarkTextPrimary
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "A calm, private operating system designed to prevent overwhelm while managing dual degrees, data science, and career.",
                                textAlign = TextAlign.Center,
                                color = GlassDarkTextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    2 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Core Focus Areas", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = GlassDarkTextPrimary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text("We follow the Max 2 Major Goals principle to protect your deep focus.", textAlign = TextAlign.Center, color = GlassDarkTextSecondary)
                            Spacer(Modifier.height(20.dp))
                            val goals = listOf(
                                "🎓 Academic CGPA Recovery (Primary)",
                                "📊 Data Science & ₹5k-6k/mo Internship (Primary)",
                                "🏋️‍♂️ Health & Physical Energy",
                                "⏳ Mindful Screen Time & Focus",
                                "🔭 Passive GATE / JAM Exploration"
                            )
                            goals.forEach { goal ->
                                GlassCard(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    contentPadding = 12.dp
                                ) {
                                    Text(goal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = GlassDarkTextPrimary)
                                }
                            }
                        }
                    }

                    3 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Dual Degree Context", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = GlassDarkTextPrimary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text("Configured with your academic structure:", textAlign = TextAlign.Center, color = GlassDarkTextSecondary)
                            Spacer(Modifier.height(20.dp))
                            GlassCard(borderGradient = Brush.linearGradient(listOf(AccentPurple.copy(alpha = 0.5f), Color(0x15FFFFFF)))) {
                                Text("ITEP — B.Sc. B.Ed. Mathematics", fontWeight = FontWeight.Bold, color = AccentPurple)
                                Text("Year 2 • Current CGPA: 6.8 • Target: 7.5", fontSize = 13.sp, color = GlassDarkTextSecondary)
                            }
                            Spacer(Modifier.height(10.dp))
                            GlassCard(borderGradient = Brush.linearGradient(listOf(AccentBlue.copy(alpha = 0.5f), Color(0x15FFFFFF)))) {
                                Text("IIT Madras — BS Data Science", fontWeight = FontWeight.Bold, color = AccentBlue)
                                Text("Year 1 • Current CGPA: 5.5 • Target: 6.5+", fontSize = 13.sp, color = GlassDarkTextSecondary)
                            }
                        }
                    }

                    4 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Priorities", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = GlassDarkTextPrimary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text("No simultaneous GATE + JAM + SSC burden.", textAlign = TextAlign.Center, color = GlassDarkTextSecondary)
                            Spacer(Modifier.height(20.dp))
                            GlassCard {
                                Text("🎯 Major Goal 1: CGPA Recovery", fontWeight = FontWeight.Bold, color = StatusGreen)
                                Text("Consistent assignment submissions and recovery of missed sessions.", fontSize = 13.sp, color = GlassDarkTextSecondary)
                            }
                            Spacer(Modifier.height(10.dp))
                            GlassCard {
                                Text("🎯 Major Goal 2: Data Science Internship", fontWeight = FontWeight.Bold, color = AccentCyan)
                                Text("Master Python/Pandas/SQL, finish Student Performance Analyzer project, secure ₹5k-6k/month role.", fontSize = 13.sp, color = GlassDarkTextSecondary)
                            }
                        }
                    }

                    5 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Smart Non-Spam Notifications", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = GlassDarkTextPrimary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text("Guaranteed max 3-5 thoughtful prompts per day.", textAlign = TextAlign.Center, color = GlassDarkTextSecondary)
                            Spacer(Modifier.height(20.dp))
                            GlassCard {
                                Text("🌅 07:30 AM — Morning AI Brief & Top 3", fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text("📚 1h before — Class & Deadline reminders", fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text("🌙 09:30 PM — Evening Reflection Check-in", fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                            }
                        }
                    }

                    6 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreenSubtle)
                                    .border(1.dp, StatusGreen.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Ready", tint = StatusGreen, modifier = Modifier.size(48.dp))
                            }
                            Spacer(Modifier.height(24.dp))
                            Text("Your Dashboard is Ready", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold), color = GlassDarkTextPrimary)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Welcome to your personal operating system, Ravi. Let us make today count.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = GlassDarkTextSecondary
                            )
                        }
                    }
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(onClick = { step-- }) {
                        Text("Back", color = GlassDarkTextSecondary)
                    }
                } else {
                    Spacer(Modifier.width(60.dp))
                }

                Button(
                    onClick = {
                        if (step < 6) step++
                        else onFinish()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(if (step == 6) "Enter Focus OS" else "Continue")
                }
            }
        }
    }
}
