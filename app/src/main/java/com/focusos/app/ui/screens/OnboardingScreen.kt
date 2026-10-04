package com.focusos.app.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.ui.components.AppleCard
import com.focusos.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Step Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                (1..6).forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i <= step) AccentBlue else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            // Step Content
            AnimatedContent(targetState = step, label = "onboardingSteps") { currentStep ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (currentStep) {
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(StatusBlueSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SelfImprovement, contentDescription = "Focus", tint = AccentBlue, modifier = Modifier.size(44.dp))
                            }
                            Spacer(Modifier.height(24.dp))
                            Text("FOCUS OS", style = MaterialTheme.typography.displayLarge, color = AccentBlue)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "“Build a better life, one day at a time.”",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "A calm, private operating system designed to prevent overwhelm while managing dual degrees, data science, and career.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        2 -> {
                            Text("Select Your Core Focus Areas", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text("We follow the Max 2 Major Goals principle to protect your focus.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))
                            val goals = listOf(
                                "🎓 Academic CGPA Recovery (Primary)",
                                "📊 Data Science & ₹5k-6k/mo Internship (Primary)",
                                "🏋️‍♂️ Health & Physical Energy",
                                "⏳ Mindful Screen Time & Focus",
                                "🔭 Passive GATE / JAM Exploration"
                            )
                            goals.forEach { goal ->
                                AppleCard(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(goal, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }

                        3 -> {
                            Text("Your Dual Degree Profile", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text("Already configured with your real academic context:", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))
                            AppleCard(borderColor = StatusPurple.copy(alpha = 0.4f)) {
                                Text("ITEP — B.Sc. B.Ed. Mathematics", fontWeight = FontWeight.Bold, color = StatusPurple)
                                Text("Year 2 • Current CGPA: 6.8 • Target: 7.5", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(10.dp))
                            AppleCard(borderColor = AccentBlue.copy(alpha = 0.4f)) {
                                Text("IIT Madras — BS Data Science", fontWeight = FontWeight.Bold, color = AccentBlue)
                                Text("Year 1 • Current CGPA: 5.5 • Target: 6.5+", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        4 -> {
                            Text("Current Primary Goals", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text("No simultaneous GATE + JAM + SSC burden.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))
                            AppleCard {
                                Text("🎯 Major Goal 1: CGPA Recovery", fontWeight = FontWeight.Bold, color = StatusGreen)
                                Text("Consistent assignment submissions and recovery of missed sessions.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(10.dp))
                            AppleCard {
                                Text("🎯 Major Goal 2: Data Science Internship", fontWeight = FontWeight.Bold, color = AccentBlue)
                                Text("Master Python/Pandas/SQL, finish Student Performance Analyzer project, secure ₹5k-6k/month role.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        5 -> {
                            Text("Smart Non-Spamming Notifications", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text("Guaranteed max 3-5 thoughtful prompts per day.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))
                            AppleCard {
                                Text("🌅 07:30 AM — Morning AI Brief & Top 3", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text("📚 1h before — Class & Deadline reminders", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text("🌙 09:30 PM — Evening Reflection Check-in", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        6 -> {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreenSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Ready", tint = StatusGreen, modifier = Modifier.size(48.dp))
                            }
                            Spacer(Modifier.height(24.dp))
                            Text("Your Dashboard is Ready", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Welcome to your personal operating system, Ravi. Let's make today count.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
