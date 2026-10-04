package com.focusos.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(700))
        delay(800)
        onSplashComplete()
    }

    GlassBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NOVA",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Focus. Progress. Become.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = AccentCyan
            )
        }
    }
}
