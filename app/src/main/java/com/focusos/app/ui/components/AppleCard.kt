package com.focusos.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focusos.app.ui.theme.*

/**
 * Ambient Glassmorphic Background with soft luminous gradient glows
 */
@Composable
fun GlassBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassBgDark,
                        Color(0xFF0F131E),
                        GlassBgDark
                    )
                )
            )
    ) {
        // Ambient Top Glow (Blue & Purple)
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-40).dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentBlue.copy(alpha = 0.16f),
                            AccentPurple.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Ambient Right Glow (Cyan & Indigo)
        Box(
            modifier = Modifier
                .offset(x = 220.dp, y = 260.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.12f),
                            StatusIndigo.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Ambient Bottom Glow (Purple & Pink)
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 560.dp)
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = 0.10f),
                            AccentBlue.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

/**
 * True Glassmorphic Card Container with translucent fill & luminous gradient stroke
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x18FFFFFF),
    borderGradient: Brush = GlassBorderGradient,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val boxModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.16f),
                        backgroundColor.copy(alpha = 0.07f)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.16f),
                        backgroundColor.copy(alpha = 0.07f)
                    )
                ),
                shape = shape
            )
    }

    Surface(
        modifier = boxModifier,
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(borderWidth, borderGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Backward-compatible AppleCard updated with Glassmorphic styling
 */
@Composable
fun AppleCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x18FFFFFF),
    borderColor: Color = Color(0x28FFFFFF),
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderBrush = Brush.linearGradient(
        listOf(
            borderColor.copy(alpha = 0.45f),
            borderColor.copy(alpha = 0.15f),
            Color(0x05FFFFFF)
        )
    )

    GlassCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        borderGradient = borderBrush,
        cornerRadius = cornerRadius,
        borderWidth = borderWidth,
        contentPadding = contentPadding,
        onClick = onClick,
        content = content
    )
}
