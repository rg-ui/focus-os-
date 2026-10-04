package com.focusos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    GlassBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Hero
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(StatusBlueSubtle)
                        .border(1.dp, AccentCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Adjust,
                        contentDescription = "NOVA",
                        tint = AccentCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "NOVA",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = GlassDarkTextPrimary
                )

                Text(
                    text = "Focus. Progress. Become.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Build a life that moves forward.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center
                    ),
                    color = GlassDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NOVA helps you organize your studies, career, health, goals and everyday life — in one focused space.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    ),
                    color = GlassDarkTextSecondary
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonShape = RoundedCornerShape(16.dp)

                // Get Started Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(buttonShape)
                        .background(
                            Brush.linearGradient(listOf(AccentBlue, AccentCyan))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.4f), buttonShape)
                        .clickable(onClick = onGetStarted),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Already Have Account Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(buttonShape)
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, Color(0x25FFFFFF), buttonShape)
                        .clickable(onClick = onLogin),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "I already have an account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlassDarkTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Your data stays private to your account.",
                    fontSize = 12.sp,
                    color = GlassDarkTextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
