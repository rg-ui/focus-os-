package com.focusos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.models.TaskCategory
import com.focusos.app.data.models.TaskPriority
import com.focusos.app.ui.theme.*

@Composable
fun CategoryBadge(category: TaskCategory, modifier: Modifier = Modifier) {
    val (bgColor, borderColor, textColor, label) = when (category) {
        TaskCategory.IITM -> Quad(StatusBlueSubtle, AccentBlue.copy(alpha = 0.35f), AccentBlue, "IITM DS")
        TaskCategory.ITEP -> Quad(StatusPurpleSubtle, AccentPurple.copy(alpha = 0.35f), AccentPurple, "ITEP Math")
        TaskCategory.DATA_SCIENCE -> Quad(StatusTealSubtle, StatusTeal.copy(alpha = 0.35f), StatusTeal, "Data Science")
        TaskCategory.INTERNSHIP -> Quad(StatusGreenSubtle, StatusGreen.copy(alpha = 0.35f), StatusGreen, "Internship")
        TaskCategory.PROJECT -> Quad(StatusOrangeSubtle, StatusOrange.copy(alpha = 0.35f), StatusOrange, "Project")
        TaskCategory.HEALTH -> Quad(StatusGreenSubtle, StatusGreen.copy(alpha = 0.35f), StatusGreen, "Health / Gym")
        TaskCategory.GATE -> Quad(StatusPurpleSubtle, AccentPurple.copy(alpha = 0.35f), AccentPurple, "GATE")
        TaskCategory.JAM -> Quad(StatusOrangeSubtle, StatusOrange.copy(alpha = 0.35f), StatusOrange, "JAM")
        TaskCategory.SSC -> Quad(StatusRedSubtle, StatusRed.copy(alpha = 0.35f), StatusRed, "SSC")
        TaskCategory.PERSONAL -> Quad(Color(0x14FFFFFF), Color(0x28FFFFFF), GlassDarkTextSecondary, "Personal")
    }

    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (bgColor, borderColor, textColor, label) = when (priority) {
        TaskPriority.HIGH -> Quad(StatusRedSubtle, StatusRed.copy(alpha = 0.35f), StatusRed, "High")
        TaskPriority.MEDIUM -> Quad(StatusOrangeSubtle, StatusOrange.copy(alpha = 0.35f), StatusOrange, "Med")
        TaskPriority.LOW -> Quad(Color(0x14FFFFFF), Color(0x28FFFFFF), GlassDarkTextSecondary, "Low")
    }

    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
