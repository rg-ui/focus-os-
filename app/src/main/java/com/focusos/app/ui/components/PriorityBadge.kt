package com.focusos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
    val (bgColor, textColor, label) = when (category) {
        TaskCategory.IITM -> Triple(StatusBlueSubtle, AccentBlue, "IITM DS")
        TaskCategory.ITEP -> Triple(StatusPurpleSubtle, StatusPurple, "ITEP Math")
        TaskCategory.DATA_SCIENCE -> Triple(StatusBlueSubtle, AccentBlue, "Data Science")
        TaskCategory.INTERNSHIP -> Triple(StatusGreenSubtle, StatusGreen, "Internship")
        TaskCategory.PROJECT -> Triple(StatusOrangeSubtle, StatusOrange, "Project")
        TaskCategory.HEALTH -> Triple(StatusGreenSubtle, StatusGreen, "Health / Gym")
        TaskCategory.GATE -> Triple(StatusPurpleSubtle, StatusPurple, "GATE")
        TaskCategory.JAM -> Triple(StatusOrangeSubtle, StatusOrange, "JAM")
        TaskCategory.SSC -> Triple(StatusRedSubtle, StatusRed, "SSC")
        TaskCategory.PERSONAL -> Triple(Color(0x1F8E8E93), MaterialTheme.colorScheme.onSurfaceVariant, "Personal")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
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
    val (bgColor, textColor, label) = when (priority) {
        TaskPriority.HIGH -> Triple(StatusRedSubtle, StatusRed, "High")
        TaskPriority.MEDIUM -> Triple(StatusOrangeSubtle, StatusOrange, "Med")
        TaskPriority.LOW -> Triple(Color(0x1F8E8E93), MaterialTheme.colorScheme.onSurfaceVariant, "Low")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
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
