package com.focusos.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.focusos.app.ui.components.*
import com.focusos.app.ui.screens.*
import com.focusos.app.ui.theme.*
import com.focusos.app.util.AppUpdateManager
import com.focusos.app.util.NotificationHelper
import com.focusos.app.util.UpdateInfo
import kotlinx.coroutines.launch

enum class AppScreenState {
    SPLASH,
    WELCOME,
    SIGN_UP,
    LOGIN,
    FORGOT_PASSWORD,
    ONBOARDING,
    MAIN_APP
}

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
            val session by repository.authSession.collectAsState()
            val userProfile by repository.userProfile.collectAsState()
            val tasks by repository.tasks.collectAsState()
            val subjects by repository.subjects.collectAsState()
            val projects by repository.portfolioProjects.collectAsState()
            val internships by repository.internships.collectAsState()

            var appScreenState by remember { mutableStateOf(AppScreenState.SPLASH) }
            var currentNav by remember { mutableStateOf(NavItem.HOME) }
            var showSettings by remember { mutableStateOf(false) }
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
                when (appScreenState) {
                    AppScreenState.SPLASH -> {
                        SplashScreen(
                            onSplashComplete = {
                                appScreenState = when {
                                    session.accessToken.isNotBlank() && userProfile.onboardingCompleted -> AppScreenState.MAIN_APP
                                    session.accessToken.isNotBlank() && !userProfile.onboardingCompleted -> AppScreenState.ONBOARDING
                                    else -> AppScreenState.WELCOME
                                }
                            }
                        )
                    }

                    AppScreenState.WELCOME -> {
                        WelcomeScreen(
                            onGetStarted = { appScreenState = AppScreenState.SIGN_UP },
                            onLogin = { appScreenState = AppScreenState.LOGIN }
                        )
                    }

                    AppScreenState.SIGN_UP -> {
                        SignUpScreen(
                            repository = repository,
                            onSignUpSuccess = {
                                appScreenState = AppScreenState.ONBOARDING
                            },
                            onNavigateToLogin = {
                                appScreenState = AppScreenState.LOGIN
                            },
                            onBack = {
                                appScreenState = AppScreenState.WELCOME
                            }
                        )
                    }

                    AppScreenState.LOGIN -> {
                        LoginScreen(
                            repository = repository,
                            onLoginSuccess = {
                                val completed = repository.userProfile.value.onboardingCompleted
                                appScreenState = if (completed) AppScreenState.MAIN_APP else AppScreenState.ONBOARDING
                            },
                            onNavigateToSignUp = {
                                appScreenState = AppScreenState.SIGN_UP
                            },
                            onForgotPassword = {
                                appScreenState = AppScreenState.FORGOT_PASSWORD
                            },
                            onBack = {
                                appScreenState = AppScreenState.WELCOME
                            }
                        )
                    }

                    AppScreenState.FORGOT_PASSWORD -> {
                        ForgotPasswordScreen(
                            repository = repository,
                            onBack = {
                                appScreenState = AppScreenState.LOGIN
                            }
                        )
                    }

                    AppScreenState.ONBOARDING -> {
                        OnboardingFlowScreen(
                            repository = repository,
                            onOnboardingFinished = {
                                appScreenState = AppScreenState.MAIN_APP
                            }
                        )
                    }

                    AppScreenState.MAIN_APP -> {
                        if (showSettings) {
                            SettingsScreen(
                                repository = repository,
                                onReplayOnboarding = {
                                    showSettings = false
                                    appScreenState = AppScreenState.ONBOARDING
                                },
                                onSignOut = {
                                    showSettings = false
                                    appScreenState = AppScreenState.WELCOME
                                },
                                onBack = { showSettings = false }
                            )
                        } else {
                            Scaffold(
                                topBar = {
                                    NovaGlassTopBar(
                                        currentNav = currentNav,
                                        onOpenSearch = { showGlobalSearch = true },
                                        onOpenSettings = { showSettings = true },
                                        onTestNotification = {
                                            NotificationHelper.showNotification(
                                                this@MainActivity,
                                                NotificationHelper.CHANNEL_DEADLINES,
                                                101,
                                                "NOVA Focus Reminder",
                                                "Time for your scheduled study session."
                                            )
                                        }
                                    )
                                },
                                bottomBar = {
                                    NovaGlassBottomNavBar(
                                        currentNav = currentNav,
                                        onNavSelected = { currentNav = it }
                                    )
                                },
                                floatingActionButton = {
                                    if (currentNav == NavItem.HOME || currentNav == NavItem.TODAY) {
                                        FloatingActionButton(
                                            onClick = { showQuickActions = true },
                                            containerColor = AccentBlue,
                                            contentColor = Color.White,
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .padding(bottom = 76.dp)
                                                .size(54.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Quick Actions",
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
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
                                            onOpenFocusTimer = { showFocusTimer = true },
                                            onAddTask = { showAddTaskDialog = true }
                                        )
                                        NavItem.TODAY -> TodayScreen(
                                            repository = repository,
                                            onOpenFocusTimer = { showFocusTimer = true }
                                        )
                                        NavItem.ACADEMICS -> AcademicsScreen(
                                            repository = repository,
                                            onNavigateToAiMentor = { currentNav = NavItem.AI }
                                        )
                                        NavItem.CAREER -> CareerScreen(
                                            repository = repository,
                                            onNavigateToAiMentor = {
                                                pendingAiPrompt = it
                                                currentNav = NavItem.AI
                                            }
                                        )
                                        NavItem.HEALTH -> HealthScreen(repository = repository)
                                        NavItem.AI -> AiMentorScreen(
                                            repository = repository,
                                            initialPrompt = pendingAiPrompt
                                        )
                                    }
                                }

                                // Update Dialog
                                availableUpdate?.let { update ->
                                    AlertDialog(
                                        onDismissRequest = { availableUpdate = null },
                                        containerColor = GlassDarkCard,
                                        title = {
                                            Text(
                                                "NOVA Update Available",
                                                color = GlassDarkTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        text = {
                                            Column {
                                                Text(
                                                    "Version ${update.latestVersion} is now ready for download.",
                                                    color = GlassDarkTextSecondary,
                                                    fontSize = 14.sp
                                                )
                                                if (update.changelog.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        update.changelog,
                                                        color = GlassDarkTextPrimary,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    AppUpdateManager.downloadAndInstallApk(
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
                                                Text("Later", color = GlassDarkTextSecondary)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaGlassTopBar(
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(StatusBlueSubtle)
                        .border(1.dp, AccentBlue.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Adjust,
                        contentDescription = "NOVA",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NOVA",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = GlassDarkTextPrimary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onTestNotification) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Alerts", tint = GlassDarkTextSecondary)
            }
            IconButton(onClick = onOpenSearch) {
                Icon(Icons.Outlined.Search, contentDescription = "Search", tint = GlassDarkTextSecondary)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile", tint = GlassDarkTextSecondary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xCC090B10)
        )
    )
}

@Composable
fun NovaGlassBottomNavBar(
    currentNav: NavItem,
    onNavSelected: (NavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xCC090B10))
            .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
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
                            tint = if (isSelected) AccentCyan else GlassDarkTextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentCyan else GlassDarkTextSecondary
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = StatusBlueSubtle
                    )
                )
            }
        }
    }
}
