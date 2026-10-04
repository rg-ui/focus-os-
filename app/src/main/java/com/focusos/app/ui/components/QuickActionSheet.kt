package com.focusos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.ui.theme.*

data class QuickActionItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionSheet(
    onDismiss: () -> Unit,
    onActionSelected: (String) -> Unit
) {
    val actions = listOf(
        QuickActionItem("add_task", "Add Task", Icons.Default.CheckCircle, AccentBlue, StatusBlueSubtle),
        QuickActionItem("log_class", "Log Class", Icons.Default.School, StatusPurple, StatusPurpleSubtle),
        QuickActionItem("start_focus", "Start Focus", Icons.Default.Timer, AccentBlue, StatusBlueSubtle),
        QuickActionItem("log_gym", "Log Gym", Icons.Default.FitnessCenter, StatusGreen, StatusGreenSubtle),
        QuickActionItem("log_study", "Log Study", Icons.Default.MenuBook, StatusOrange, StatusOrangeSubtle),
        QuickActionItem("add_internship", "Add Internship", Icons.Default.Work, StatusTeal, Color(0x1F64D2FF)),
        QuickActionItem("add_journal", "Daily Journal", Icons.Default.EditNote, StatusIndigo, Color(0x1F5E5CE6)),
        QuickActionItem("update_cgpa", "Update CGPA", Icons.Default.TrendingUp, StatusGreen, StatusGreenSubtle),
        QuickActionItem("chat_ai", "Ask AI Mentor", Icons.Default.Psychology, AccentBlue, StatusBlueSubtle)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Capture anything in seconds without losing context",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(actions) { action ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onActionSelected(action.id)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(action.bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.title,
                                tint = action.iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = action.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
