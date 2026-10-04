package com.focusos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.ui.theme.*

@Composable
fun AppleScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 110.dp,
    strokeWidth: Dp = 10.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (score / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "scoreRingProgress"
    )

    val progressBrush = when {
        score >= 80 -> Brush.sweepGradient(listOf(StatusGreen, AccentCyan, StatusGreen))
        score >= 60 -> Brush.sweepGradient(listOf(AccentBlue, AccentCyan, AccentBlue))
        score >= 30 -> Brush.sweepGradient(listOf(StatusOrange, AccentBlue, StatusOrange))
        else -> Brush.sweepGradient(listOf(AccentPurple, AccentBlue, AccentPurple))
    }

    val trackColor = Color(0x18FFFFFF)

    Box(
        modifier = modifier
            .size(ringSize)
            .clip(CircleShape)
            .background(Color(0x0CFFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val strokePx = strokeWidth.toPx()
            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            // Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = progressBrush,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = GlassDarkTextPrimary
            )
            Text(
                text = "/ 100",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GlassDarkTextSecondary
            )
        }
    }
}
