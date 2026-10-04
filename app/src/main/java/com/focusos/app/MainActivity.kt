package com.focusos.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.models.TaskCategory
import com.focusos.app.data.models.TaskItem
import com.focusos.app.data.models.TaskPriority
import com.focusos.app.ui.components.*
import com.focusos.app.ui.screens.*
import com.focusos.app.ui.theme.AccentBlue
import com.focusos.app.ui.theme.FocusOsTheme
import com.focusos.app.ui.theme.StatusBlueSubtle
import com.focusos.app.ui.theme.StatusGreen
import com.focusos.app.util.AppUpdateManager
import com.focusos.app.util.NotificationHelper
import com.focusos.app.util.UpdateInfo
import kotlinx.coroutines.launch

enum class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    TODAY("Today", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday),
    ACADEMICS("Academics", Icons.Filled.School, Icons.Outlined.School),
    CAREER("Career", Icons.Filled.Work, Icons.Outlined.WorkOutline),
    HEALTH("Health", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    AI("AI", Icons.Filled.Psychology, Icons.Outlined.Psychology)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val repository = (application as FocusOsApp).repository
            val userProfile by repository.userProfile.collectAsState()
            val tasks by repository.tasks.collectAsState()
            val subjects by repository.subjects.collectAsState()
            val projects by repository.portfolioProjects.collectAsState()
            val internships by repository.internships.collectAsState()

            var currentNav by remember { mutableStateOf(NavItem.HOME) }
            var showSettings by remember { mutableStateOf(false) }
            var showOnboarding by remember { mutableStateOf(!userProfile.onboardingCompleted) }
            var showQuickActions by remember { mutableStateOf(false) }
            var showFocusTimer by remember { mutableStateOf(false) }
            var showGlobalSearch by remember { mutableStateOf(false) }
            var pendingAiPrompt by remember { mutableStateOf<String?>(null) }
            var showAddTaskDialog by remember { mutableStateOf(false) }

            var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
            val coroutineScope = rememberCoroutineScope()

            // Auto-check for OTA updates on launch
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    val result = AppUpdateManager.checkForUpdates()
                    if (result.isSuccess) {
                        val update = result.getOrNull()
                        if (update != null && update.hasUpdate) {
                            availableUpdate = update
                        }
                    }
                }
            }

            FocusOsTheme {
                if (showOnboarding) {
                    OnboardingScreen(
                        onFinish = {
                            repository.completeOnboarding()
                            showOnboarding = false
                        }
                    )
                } else if (showSettings) {
                    SettingsScreen(
                        repository = repository,
                        onReplayOnboarding = {
                            showSettings = false
                            showOnboarding = true
                        },
                        onBack = { showSettings = false }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            FocusOsTopBar(
                                currentNav = currentNav,
                                onOpenSearch = { showGlobalSearch = true },
                                onOpenSettings = { showSettings = true },
                                onTestNotification = {
                                    NotificationHelper.showNotification(
                                        this@MainActivity,
                                        NotificationHelper.CHANNEL_DEADLINES,
                                        101,
                                        "IITM Statistics Assignment",
                                        "Due tomorrow at 11:59 PM. Finish this before starting new topics."
                                    )
                                    Toast.makeText(this@MainActivity, "Notification sent", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        bottomBar = {
                            FocusOsBottomNavBar(
                                currentNav = currentNav,
                                onNavSelected = { currentNav = it }
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { showQuickActions = true },
                                containerColor = AccentBlue,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(bottom = 70.dp)
                                    .size(56.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Quick Action", modifier = Modifier.size(28.dp))
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentNav) {
                                NavItem.HOME -> HomeScreen(
                                    repository = repository,
                                    onNavigateToToday = { currentNav = NavItem.TODAY },
                                    onNavigateToAcademics = { currentNav = NavItem.ACADEMICS },
                                    onNavigateToCareer = { currentNav = NavItem.CAREER },
                                    onNavigateToHealth = { currentNav = NavItem.HEALTH },
                                    onNavigateToAi = { currentNav = NavItem.AI },
                                    onOpenFocusTimer = { showFocusTimer = true }
                                )
                                NavItem.TODAY -> TodayScreen(
                                    repository = repository,
                                    onOpenFocusTimer = { showFocusTimer = true }
                                )
                                NavItem.ACADEMICS -> AcademicsScreen(
                                    repository = repository
                                )
                                NavItem.CAREER -> CareerScreen(
                                    repository = repository,
                                    onNavigateToAiMentor = { prompt ->
                                        pendingAiPrompt = prompt
                                        currentNav = NavItem.AI
                                    }
                                )
                                NavItem.HEALTH -> HealthScreen(
                                    repository = repository
                                )
                                NavItem.AI -> AiMentorScreen(
                                    repository = repository,
                                    initialPrompt = pendingAiPrompt
                                )
                            }
                        }

                        // MODALS & OVERLAYS
                        if (availableUpdate != null) {
                            val update = availableUpdate!!
                            AlertDialog(
                                onDismissRequest = { availableUpdate = null },
                                title = { Text("Update Available (${update.latestVersionName})") },
                                text = {
                                    Column {
                                        Text(
                                            text = update.changelog,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = "Focus OS will download and apply this update automatically without losing your data.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = StatusGreen
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            AppUpdateManager.startDownloadAndInstall(
                                                this@MainActivity,
                                                update.downloadUrl
                                            ) { msg ->
                                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                            }
                                            availableUpdate = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                    ) {
                                        Text("Update Now")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { availableUpdate = null }) {
                                        Text("Later")
                                    }
                                }
                            )
                        }

                        if (showQuickActions) {
                            QuickActionSheet(
                                onDismiss = { showQuickActions = false },
                                onActionSelected = { actionId ->
                                    when (actionId) {
                                        "add_task" -> showAddTaskDialog = true
                                        "start_focus" -> showFocusTimer = true
                                        "log_class" -> currentNav = NavItem.TODAY
                                        "log_gym" -> currentNav = NavItem.HEALTH
                                        "log_study" -> showFocusTimer = true
                                        "add_internship" -> currentNav = NavItem.CAREER
                                        "add_journal" -> currentNav = NavItem.HEALTH
                                        "update_cgpa" -> currentNav = NavItem.ACADEMICS
                                        "chat_ai" -> currentNav = NavItem.AI
                                    }
                                }
                            )
                        }

                        if (showFocusTimer) {
                            FocusTimerModal(
                                onDismiss = { showFocusTimer = false },
                                onSessionCompleted = { session ->
                                    repository.logFocusSession(session)
                                    Toast.makeText(this@MainActivity, "Focus session logged! +${session.durationMinutes}m", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        if (showGlobalSearch) {
                            GlobalSearchDialog(
                                tasks = tasks,
                                subjects = subjects,
                                projects = projects,
                                internships = internships,
                                onDismiss = { showGlobalSearch = false },
                                onItemSelected = { itemId ->
                                    when {
                                        itemId.startsWith("task_") -> currentNav = NavItem.TODAY
                                        itemId.startsWith("subject_") -> currentNav = NavItem.ACADEMICS
                                        itemId.startsWith("project_") || itemId.startsWith("intern_") -> currentNav = NavItem.CAREER
                                    }
                                }
                            )
                        }

                        if (showAddTaskDialog) {
                            AddTaskDialog(
                                onDismiss = { showAddTaskDialog = false },
                                onAddTask = { newTask ->
                                    repository.addTask(newTask)
                                    showAddTaskDialog = false
                                    Toast.makeText(this@MainActivity, "Task added", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusOsTopBar(
    currentNav: NavItem,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onTestNotification: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(StatusBlueSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Adjust,
                        contentDescription = "Focus OS",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FOCUS OS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onTestNotification) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Alerts", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onOpenSearch) {
                Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun FocusOsBottomNavBar(
    currentNav: NavItem,
    onNavSelected: (NavItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(72.dp)
    ) {
        NavItem.values().forEach { item ->
            val isSelected = currentNav == item
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = StatusBlueSubtle
                )
            )
        }
    }
}
