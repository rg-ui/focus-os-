package com.focusos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.models.DegreeInfo
import com.focusos.app.data.models.DegreeType
import com.focusos.app.data.models.UserProfile
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*

data class EducationDraft(
    var institution: String = "",
    var degreeName: String = "",
    var currentYear: Int = 1,
    var currentCgpa: Double = 7.0,
    var targetCgpa: Double = 8.0,
    var degreeType: DegreeType = DegreeType.ITEP
)

@Composable
fun OnboardingFlowScreen(
    repository: FocusOsRepository,
    onOnboardingFinished: () -> Unit
) {
    val initialProfile = repository.userProfile.collectAsState().value
    var step by remember { mutableStateOf(1) }

    // Step 1: About You
    var name by remember { mutableStateOf(initialProfile.name.ifBlank { "" }) }
    var userType by remember { mutableStateOf("College student") }

    // Step 2: Education (supports multi-degree)
    var educationList by remember {
        mutableStateOf(
            listOf(
                EducationDraft(
                    institution = "University / College",
                    degreeName = "B.Sc. / B.Tech / Degree",
                    currentYear = 2,
                    currentCgpa = 7.0,
                    targetCgpa = 8.0,
                    degreeType = DegreeType.ITEP
                )
            )
        )
    }

    // Step 3: What Matters Right Now?
    var selectedPriorities by remember {
        mutableStateOf(
            listOf("Academics", "Career", "Coding", "Focus")
        )
    }

    // Step 4: Goals
    var userGoals by remember {
        mutableStateOf(
            listOf("Improve my CGPA", "Build strong career skills", "Maintain consistent study routine")
        )
    }
    var customGoalInput by remember { mutableStateOf("") }

    // Step 5: Routine
    var wakeUpTime by remember { mutableStateOf("07:00 AM") }
    var sleepTime by remember { mutableStateOf("11:00 PM") }
    var preferredStudyHours by remember { mutableStateOf("Evenings (4:00 PM - 8:00 PM)") }
    var gymPreference by remember { mutableStateOf("Evening workout (4-5 days/wk)") }
    var reminderStyle by remember { mutableStateOf("Balanced") }

    // Step 6: Focus Problems
    var selectedChallenges by remember {
        mutableStateOf(
            listOf("Phone & Social Media", "Procrastination", "Lack of Consistency")
        )
    }

    GlassBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Progress Indicator (1 / 7)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        IconButton(onClick = { step -= 1 }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassDarkTextPrimary)
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Text(
                        text = "$step / 7",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AccentCyan
                    )

                    if (step in 2..6) {
                        Text(
                            text = "Skip",
                            fontSize = 13.sp,
                            color = GlassDarkTextSecondary,
                            modifier = Modifier
                                .clickable { step += 1 }
                                .padding(8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Step Progress Bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..7).forEach { i ->
                        val isDone = i <= step
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isDone) AccentCyan else Color(0x20FFFFFF))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Content by Step
            Box(modifier = Modifier.weight(1f)) {
                when (step) {
                    1 -> Step1AboutYou(
                        name = name,
                        onNameChange = { name = it },
                        userType = userType,
                        onUserTypeSelect = { userType = it }
                    )
                    2 -> Step2Education(
                        educationList = educationList,
                        onEducationUpdate = { educationList = it }
                    )
                    3 -> Step3Priorities(
                        selected = selectedPriorities,
                        onToggle = { tag ->
                            selectedPriorities = if (selectedPriorities.contains(tag)) {
                                selectedPriorities - tag
                            } else {
                                selectedPriorities + tag
                            }
                        }
                    )
                    4 -> Step4Goals(
                        goals = userGoals,
                        onToggleSuggestion = { goal ->
                            userGoals = if (userGoals.contains(goal)) userGoals - goal else userGoals + goal
                        },
                        customGoal = customGoalInput,
                        onCustomGoalChange = { customGoalInput = it },
                        onAddCustomGoal = {
                            if (customGoalInput.isNotBlank()) {
                                userGoals = userGoals + customGoalInput.trim()
                                customGoalInput = ""
                            }
                        }
                    )
                    5 -> Step5Routine(
                        wakeTime = wakeUpTime,
                        onWakeChange = { wakeUpTime = it },
                        sleepTime = sleepTime,
                        onSleepChange = { sleepTime = it },
                        studyHours = preferredStudyHours,
                        onStudyHoursChange = { preferredStudyHours = it },
                        gymPref = gymPreference,
                        onGymPrefChange = { gymPreference = it },
                        reminderStyle = reminderStyle,
                        onReminderStyleChange = { reminderStyle = it }
                    )
                    6 -> Step6Challenges(
                        selected = selectedChallenges,
                        onToggle = { challenge ->
                            selectedChallenges = if (selectedChallenges.contains(challenge)) {
                                selectedChallenges - challenge
                            } else {
                                selectedChallenges + challenge
                            }
                        }
                    )
                    7 -> Step7Summary(
                        name = name.ifBlank { "Student" },
                        userType = userType,
                        priorities = selectedPriorities,
                        goals = userGoals,
                        education = educationList,
                        routine = "$preferredStudyHours • $gymPreference"
                    )
                }
            }

            // Bottom CTA Button
            val buttonShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(buttonShape)
                    .background(Brush.linearGradient(listOf(AccentBlue, AccentCyan)))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), buttonShape)
                    .clickable {
                        if (step < 7) {
                            step += 1
                        } else {
                            // Convert EducationDraft list to DegreeInfo models
                            val createdDegrees = educationList.mapIndexed { index, draft ->
                                DegreeInfo(
                                    type = if (index == 0) DegreeType.ITEP else DegreeType.IITM,
                                    name = draft.degreeName.ifBlank { "Academic Program" },
                                    currentYear = draft.currentYear,
                                    totalYears = 4,
                                    currentCgpa = draft.currentCgpa,
                                    targetCgpa = draft.targetCgpa,
                                    completedCredits = draft.currentYear * 20,
                                    totalCredits = 80,
                                    currentSemester = (draft.currentYear * 2) - 1,
                                    isPrimaryFocus = index == 0
                                )
                            }

                            val finalProfile = UserProfile(
                                id = initialProfile.id,
                                name = name.ifBlank { "User" },
                                email = initialProfile.email,
                                userType = userType,
                                onboardingCompleted = true,
                                priorities = selectedPriorities,
                                goals = userGoals,
                                challenges = selectedChallenges,
                                wakeUpTime = wakeUpTime,
                                sleepTime = sleepTime,
                                preferredStudyHours = preferredStudyHours,
                                gymPreference = gymPreference,
                                reminderStyle = reminderStyle,
                                majorGoal1 = userGoals.getOrNull(0) ?: "CGPA Recovery & Excellence",
                                majorGoal2 = userGoals.getOrNull(1) ?: "Skill Mastery & Career"
                            )

                            repository.completeOnboarding(finalProfile, createdDegrees, userGoals)
                            onOnboardingFinished()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (step == 7) "Build My NOVA" else "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ----------------- STEP COMPOSABLES -----------------

@Composable
private fun Step1AboutYou(
    name: String,
    onNameChange: (String) -> Unit,
    userType: String,
    onUserTypeSelect: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(
                text = "Let's start with you.",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "What should NOVA call you?",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Your First Name or Nickname", color = GlassDarkTextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Color(0x25FFFFFF),
                    focusedContainerColor = Color(0x12FFFFFF),
                    unfocusedContainerColor = Color(0x12FFFFFF),
                    focusedTextColor = GlassDarkTextPrimary,
                    unfocusedTextColor = GlassDarkTextPrimary
                )
            )
        }

        item {
            Text(
                text = "What best describes you?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassDarkTextPrimary
            )
        }

        val types = listOf(
            "College student",
            "University student",
            "School student",
            "Working professional",
            "Founder / Builder",
            "Freelancer",
            "Other"
        )

        items(types) { type ->
            val isSelected = userType == type
            val shape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isSelected) AccentBlue.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                    .border(1.dp, if (isSelected) AccentCyan else Color(0x18FFFFFF), shape)
                    .clickable { onUserTypeSelect(type) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = type,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GlassDarkTextPrimary else GlassDarkTextSecondary
                    )
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Step2Education(
    educationList: List<EducationDraft>,
    onEducationUpdate: (List<EducationDraft>) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(
                text = "What are you studying?",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NOVA adapts to single or dual degree academic workloads.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )
        }

        items(educationList.indices.toList()) { index ->
            val item = educationList[index]
            GlassCard {
                Text(
                    text = "PROGRAM #${index + 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = item.degreeName,
                    onValueChange = { newName ->
                        val updated = educationList.toMutableList()
                        updated[index] = item.copy(degreeName = newName)
                        onEducationUpdate(updated)
                    },
                    label = { Text("Degree / Program (e.g. BS Data Science, B.Sc Maths)", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = item.institution,
                    onValueChange = { newInst ->
                        val updated = educationList.toMutableList()
                        updated[index] = item.copy(institution = newInst)
                        onEducationUpdate(updated)
                    },
                    label = { Text("Institution / College Name", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = "${item.currentCgpa}",
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: item.currentCgpa
                            val updated = educationList.toMutableList()
                            updated[index] = item.copy(currentCgpa = v)
                            onEducationUpdate(updated)
                        },
                        label = { Text("Current CGPA") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = "${item.targetCgpa}",
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: item.targetCgpa
                            val updated = educationList.toMutableList()
                            updated[index] = item.copy(targetCgpa = v)
                            onEducationUpdate(updated)
                        },
                        label = { Text("Target CGPA") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x12FFFFFF))
                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(12.dp))
                    .clickable {
                        onEducationUpdate(
                            educationList + EducationDraft(
                                institution = "IIT Madras / Online",
                                degreeName = "BS in Data Science",
                                currentYear = 1,
                                currentCgpa = 6.5,
                                targetCgpa = 7.5,
                                degreeType = DegreeType.IITM
                            )
                        )
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Another Degree / Program", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AccentCyan)
                }
            }
        }
    }
}

@Composable
private fun Step3Priorities(
    selected: List<String>,
    onToggle: (String) -> Unit
) {
    val allPriorities = listOf(
        "Academics", "Career", "Coding", "Data Science",
        "AI/ML", "Competitive exams", "Internship", "Job preparation",
        "Startup / Projects", "Fitness", "Health", "Productivity",
        "Focus & Deep Work", "Personal growth"
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(
                text = "What matters right now?",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select all areas you want NOVA to organize and prioritize.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allPriorities.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            val isSel = selected.contains(item)
                            val shape = RoundedCornerShape(12.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape)
                                    .background(if (isSel) AccentBlue.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                                    .border(1.dp, if (isSel) AccentCyan else Color(0x18FFFFFF), shape)
                                    .clickable { onToggle(item) }
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSel) "$item ✓" else item,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) AccentCyan else GlassDarkTextPrimary
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4Goals(
    goals: List<String>,
    onToggleSuggestion: (String) -> Unit,
    customGoal: String,
    onCustomGoalChange: (String) -> Unit,
    onAddCustomGoal: () -> Unit
) {
    val suggestions = listOf(
        "Improve my CGPA",
        "Get a paid internship",
        "Learn Python & Data Science",
        "Prepare for GATE / Exams",
        "Hit the gym consistently",
        "Stop mindless scrolling",
        "Build a daily study rhythm"
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(
                text = "What do you want to achieve?",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick 3–5 active goals for maximum clarity without overwhelm.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customGoal,
                    onValueChange = onCustomGoalChange,
                    placeholder = { Text("Type custom goal (e.g. Build portfolio)", fontSize = 13.sp, color = GlassDarkTextSecondary) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAddCustomGoal,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Add")
                }
            }
        }

        item {
            Text("Suggestions:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextSecondary)
        }

        items(suggestions) { sug ->
            val isAdded = goals.contains(sug)
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isAdded) StatusGreenSubtle else Color(0x12FFFFFF))
                    .border(1.dp, if (isAdded) StatusGreen.copy(alpha = 0.5f) else Color(0x18FFFFFF), shape)
                    .clickable { onToggleSuggestion(sug) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sug,
                        fontSize = 13.sp,
                        fontWeight = if (isAdded) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAdded) GlassDarkTextPrimary else GlassDarkTextSecondary
                    )
                    Text(
                        text = if (isAdded) "Added ✓" else "+ Select",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAdded) StatusGreen else AccentCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun Step5Routine(
    wakeTime: String,
    onWakeChange: (String) -> Unit,
    sleepTime: String,
    onSleepChange: (String) -> Unit,
    studyHours: String,
    onStudyHoursChange: (String) -> Unit,
    gymPref: String,
    onGymPrefChange: (String) -> Unit,
    reminderStyle: String,
    onReminderStyleChange: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(
                text = "Your Ideal Routine",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Help NOVA calibrate notifications and study blocks.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )
        }

        item {
            GlassCard {
                Text("SLEEP & WAKE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = onWakeChange,
                        label = { Text("Wake-up Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = sleepTime,
                        onValueChange = onSleepChange,
                        label = { Text("Sleep Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        item {
            GlassCard {
                Text("STUDY & WORK PREFERENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = studyHours,
                    onValueChange = onStudyHoursChange,
                    label = { Text("Preferred Deep Work Slot") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        item {
            GlassCard {
                Text("REMINDER STYLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusTeal)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Minimal", "Balanced", "Motivational").forEach { style ->
                        val isSel = reminderStyle == style
                        val shape = RoundedCornerShape(8.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .background(if (isSel) StatusTeal.copy(alpha = 0.25f) else Color(0x10FFFFFF))
                                .border(1.dp, if (isSel) StatusTeal else Color(0x18FFFFFF), shape)
                            .clickable { onReminderStyleChange(style) }
                            .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(style, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isSel) StatusTeal else GlassDarkTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step6Challenges(
    selected: List<String>,
    onToggle: (String) -> Unit
) {
    val challenges = listOf(
        "Phone & Social Media", "Procrastination", "Lack of Consistency",
        "Too Many Competing Goals", "Poor Sleep / Energy", "Missed Classes & Catch-up",
        "Lack of Clear Daily Plan", "Exam / Career Anxiety"
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(
                text = "What's getting in your way?",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NOVA uses this to suggest frictionless nudges when you drift off course.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )
        }

        items(challenges) { chal ->
            val isSel = selected.contains(chal)
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isSel) StatusOrangeSubtle else Color(0x12FFFFFF))
                    .border(1.dp, if (isSel) StatusOrange.copy(alpha = 0.5f) else Color(0x18FFFFFF), shape)
                    .clickable { onToggle(chal) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chal,
                        fontSize = 14.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) GlassDarkTextPrimary else GlassDarkTextSecondary
                    )
                    if (isSel) {
                        Text("Selected ✓", fontSize = 12.sp, color = StatusOrange, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step7Summary(
    name: String,
    userType: String,
    priorities: List<String>,
    goals: List<String>,
    education: List<EducationDraft>,
    routine: String
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(StatusGreenSubtle)
                    .border(1.dp, StatusGreen.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = StatusGreen, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Your NOVA is ready, $name.",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassDarkTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "A private operating system configured specifically for your goals.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )
        }

        item {
            GlassCard {
                Text("YOUR FOCUS CONTEXT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Profile: $userType", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                Text("Priorities: ${priorities.joinToString(", ")}", fontSize = 12.sp, color = GlassDarkTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Key Goals:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlassDarkTextPrimary)
                goals.take(3).forEach { g ->
                    Text("• $g", fontSize = 12.sp, color = GlassDarkTextSecondary)
                }
            }
        }

        item {
            GlassCard {
                Text("ACADEMIC STRUCTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                Spacer(modifier = Modifier.height(8.dp))
                education.forEach { edu ->
                    Text("• ${edu.degreeName} (Target: ${edu.targetCgpa} CGPA)", fontSize = 12.sp, color = GlassDarkTextPrimary)
                }
            }
        }
    }
}
