package com.focusos.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.focusos.app.data.models.*
import com.focusos.app.data.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FocusOsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_os_prefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val repoScope = CoroutineScope(Dispatchers.IO)

    // Default Supabase configuration
    val defaultSupabaseUrl = "https://yvhzfrhtvwsnmzcruyyk.supabase.co"
    val defaultSupabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl2aHpmcmh0dndzbm16Y3J1eXlrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTExMTY1MTksImV4cCI6MjEwNjY5MjUxOX0.GmDiVkrVt5iYsEe7dOkaC2Wt2Z4OxJ0OxTF_YR-OduA"

    val supabaseClient = SupabaseClient(defaultSupabaseUrl, defaultSupabaseAnonKey)

    // ----------------- Auth State -----------------
    private val _authSession = MutableStateFlow(loadStoredSession())
    val authSession: StateFlow<AuthSession> = _authSession.asStateFlow()

    // ----------------- User Profile & Context -----------------
    private val _userProfile = MutableStateFlow(loadStoredProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _appSettings = MutableStateFlow(loadStoredSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // ----------------- Academic & Career Reactive Flows -----------------
    private val _degrees = MutableStateFlow(loadStoredDegrees())
    val degrees: StateFlow<List<DegreeInfo>> = _degrees.asStateFlow()

    private val _subjects = MutableStateFlow(loadStoredSubjects())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _classes = MutableStateFlow(loadStoredClasses())
    val classes: StateFlow<List<ClassSession>> = _classes.asStateFlow()

    private val _missedRecoveries = MutableStateFlow<List<MissedClassRecovery>>(emptyList())
    val missedRecoveries: StateFlow<List<MissedClassRecovery>> = _missedRecoveries.asStateFlow()

    private val _tasks = MutableStateFlow(loadStoredTasks())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _focusSessions = MutableStateFlow<List<FocusSession>>(emptyList())
    val focusSessions: StateFlow<List<FocusSession>> = _focusSessions.asStateFlow()

    private val _dsRoadmap = MutableStateFlow(loadInitialRoadmap())
    val dsRoadmap: StateFlow<List<RoadmapStage>> = _dsRoadmap.asStateFlow()

    private val _portfolioProjects = MutableStateFlow(loadInitialProjects())
    val portfolioProjects: StateFlow<List<PortfolioProject>> = _portfolioProjects.asStateFlow()

    private val _internships = MutableStateFlow<List<InternshipApplication>>(emptyList())
    val internships: StateFlow<List<InternshipApplication>> = _internships.asStateFlow()

    private val _examTracks = MutableStateFlow(loadInitialExams())
    val examTracks: StateFlow<List<ExamTrack>> = _examTracks.asStateFlow()

    private val _healthLog = MutableStateFlow(HealthLog(date = "Today", gymCompleted = false, workoutDurationMinutes = 0, steps = 0, sleepHours = 0.0, waterGlasses = 0))
    val healthLog: StateFlow<HealthLog> = _healthLog.asStateFlow()

    private val _habits = MutableStateFlow(loadInitialHabits())
    val habits: StateFlow<List<HabitItem>> = _habits.asStateFlow()

    private val _distractionLog = MutableStateFlow(DistractionLog(date = "Today", instagramMinutes = 0, youtubeMinutes = 0, gamingMinutes = 0, otherMinutes = 0, yesterdayTotalMinutes = 0))
    val distractionLog: StateFlow<DistractionLog> = _distractionLog.asStateFlow()

    private val _journal = MutableStateFlow(DailyJournal(id = "j_today", date = "Today", whatWentWell = "", whatWentWrong = "", whatToImproveTomorrow = ""))
    val journal: StateFlow<DailyJournal> = _journal.asStateFlow()

    private val _chatMessages = MutableStateFlow(loadInitialChatMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _morningBrief = MutableStateFlow(MorningBrief())
    val morningBrief: StateFlow<MorningBrief> = _morningBrief.asStateFlow()

    init {
        // Configure Supabase client with stored token if available
        val session = _authSession.value
        if (session.isLoggedIn && session.accessToken.isNotBlank()) {
            supabaseClient.setAccessToken(session.accessToken)
        }
    }

    // ----------------- Authentication API -----------------
    suspend fun signUp(name: String, email: String, pass: String): Result<AuthSession> {
        val result = supabaseClient.signUp(email = email.trim(), password = pass, name = name.trim())
        if (result.isSuccess) {
            val session = result.getOrThrow()
            saveSession(session)
            val newProfile = UserProfile(
                id = session.userId,
                name = name.trim(),
                email = email.trim(),
                onboardingCompleted = false
            )
            saveProfile(newProfile)
            _chatMessages.value = loadInitialChatMessages(name.trim())
        }
        return result
    }

    suspend fun signIn(email: String, pass: String): Result<AuthSession> {
        val result = supabaseClient.signIn(email = email.trim(), password = pass)
        if (result.isSuccess) {
            val session = result.getOrThrow()
            saveSession(session)

            // Try fetching profile from Supabase
            repoScope.launch {
                val remoteProfileRes = supabaseClient.fetchProfileFromSupabase(session.userId)
                if (remoteProfileRes.isSuccess && remoteProfileRes.getOrNull() != null) {
                    val remoteProfile = remoteProfileRes.getOrNull()!!
                    saveProfile(remoteProfile)
                } else {
                    val currentProf = _userProfile.value
                    if (currentProf.id != session.userId) {
                        saveProfile(currentProf.copy(id = session.userId, name = session.name, email = session.email))
                    }
                }
            }
            _chatMessages.value = loadInitialChatMessages(session.name)
        }
        return result
    }

    suspend fun resetPassword(email: String): Result<Boolean> {
        return supabaseClient.resetPassword(email.trim())
    }

    fun signOut() {
        supabaseClient.setAccessToken("")
        val emptySession = AuthSession()
        saveSession(emptySession)
        val emptyProfile = UserProfile()
        saveProfile(emptyProfile)
        _degrees.value = emptyList()
        _tasks.value = emptyList()
        _classes.value = emptyList()
        _missedRecoveries.value = emptyList()
        _focusSessions.value = emptyList()
        _internships.value = emptyList()
        _chatMessages.value = loadInitialChatMessages()
    }

    // ----------------- Onboarding & Personal Context Completion -----------------
    fun completeOnboarding(
        updatedProfile: UserProfile,
        userDegrees: List<DegreeInfo>,
        userGoals: List<String>
    ) {
        val finalProfile = updatedProfile.copy(
            onboardingCompleted = true,
            goals = userGoals
        )
        saveProfile(finalProfile)

        if (userDegrees.isNotEmpty()) {
            _degrees.value = userDegrees
            saveDegrees(userDegrees)
        }

        // Generate baseline subjects if academic priority is selected
        if (userDegrees.isNotEmpty() && _subjects.value.isEmpty()) {
            val generatedSubjects = generateInitialSubjectsForDegrees(userDegrees)
            _subjects.value = generatedSubjects
            saveSubjects(generatedSubjects)
        }

        // Sync with Supabase cloud
        repoScope.launch {
            supabaseClient.saveProfileToSupabase(finalProfile)
        }

        _chatMessages.value = loadInitialChatMessages(finalProfile.name)
    }

    fun updateProfile(profile: UserProfile) {
        saveProfile(profile)
        recalculateTodayScore()
        repoScope.launch {
            supabaseClient.saveProfileToSupabase(profile)
        }
    }

    fun updateSettings(settings: AppSettings) {
        _appSettings.value = settings
        prefs.edit().putString("app_settings_json", json.encodeToString(settings)).apply()
        if (settings.supabaseUrl.isNotBlank() && settings.supabaseAnonKey.isNotBlank()) {
            supabaseClient.updateConfig(settings.supabaseUrl, settings.supabaseAnonKey)
        }
    }

    // ----------------- Academic Operations -----------------
    fun addDegree(degree: DegreeInfo) {
        val updated = _degrees.value + degree
        _degrees.value = updated
        saveDegrees(updated)
    }

    fun updateDegree(type: DegreeType, currentCgpa: Double, targetCgpa: Double, completedCredits: Int) {
        val updated = _degrees.value.map {
            if (it.type == type) {
                it.copy(currentCgpa = currentCgpa, targetCgpa = targetCgpa, completedCredits = completedCredits)
            } else it
        }
        _degrees.value = updated
        saveDegrees(updated)
    }

    fun addSubject(subject: Subject) {
        val updated = _subjects.value + subject
        _subjects.value = updated
        saveSubjects(updated)
    }

    fun updateSubjectStatus(subjectId: String, newStatus: SubjectStatus, newScore: Double? = null) {
        val updated = _subjects.value.map {
            if (it.id == subjectId) {
                it.copy(status = newStatus, currentScore = newScore ?: it.currentScore)
            } else it
        }
        _subjects.value = updated
        saveSubjects(updated)
        recalculateTodayScore()
    }

    fun markClassAttendance(classId: String, status: ClassAttendanceStatus) {
        val updated = _classes.value.map {
            if (it.id == classId) it.copy(status = status) else it
        }
        _classes.value = updated

        if (status == ClassAttendanceStatus.ABSENT) {
            val session = _classes.value.find { it.id == classId } ?: return
            val recovery = MissedClassRecovery(
                id = "missed_${System.currentTimeMillis()}",
                classSessionId = session.id,
                subjectName = session.subjectName,
                degreeType = session.degreeType,
                topic = session.topic,
                missedDate = session.date
            )
            _missedRecoveries.value = listOf(recovery) + _missedRecoveries.value
        }
    }

    fun toggleRecoveryStep(recoveryId: String, stepIndex: Int) {
        val updated = _missedRecoveries.value.map { rec ->
            if (rec.id == recoveryId) {
                val modified = when (stepIndex) {
                    0 -> rec.copy(watchLectureDone = !rec.watchLectureDone)
                    1 -> rec.copy(notesDone = !rec.notesDone)
                    2 -> rec.copy(quizDone = !rec.quizDone)
                    3 -> rec.copy(revisionDone = !rec.revisionDone)
                    else -> rec
                }
                val allDone = modified.watchLectureDone && modified.notesDone && modified.quizDone && modified.revisionDone
                modified.copy(isRecovered = allDone)
            } else rec
        }
        _missedRecoveries.value = updated
        recalculateTodayScore()
    }

    // ----------------- Tasks Operations -----------------
    fun addTask(task: TaskItem) {
        val updated = listOf(task) + _tasks.value
        _tasks.value = updated
        saveTasks(updated)
        recalculateTodayScore()
    }

    fun toggleTask(taskId: String) {
        val updated = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        _tasks.value = updated
        saveTasks(updated)
        recalculateTodayScore()
    }

    fun deleteTask(taskId: String) {
        val updated = _tasks.value.filter { it.id != taskId }
        _tasks.value = updated
        saveTasks(updated)
        recalculateTodayScore()
    }

    // ----------------- Focus Operations -----------------
    fun logFocusSession(session: FocusSession) {
        _focusSessions.value = listOf(session) + _focusSessions.value
        val addedHours = session.durationMinutes / 60.0
        val currentProfile = _userProfile.value
        val updated = currentProfile.copy(
            totalFocusedHoursThisWeek = currentProfile.totalFocusedHoursThisWeek + addedHours
        )
        saveProfile(updated)
        recalculateTodayScore()
    }

    // ----------------- Career / Projects / Internships -----------------
    fun updateRoadmapStageProgress(stageNumber: Int, newProgress: Int, lessonsDone: Int, problemsDone: Int) {
        val updated = _dsRoadmap.value.map {
            if (it.stageNumber == stageNumber) {
                it.copy(
                    progressPercent = newProgress.coerceIn(0, 100),
                    lessonsCompleted = lessonsDone,
                    practiceProblemsDone = problemsDone,
                    isCompleted = newProgress >= 100
                )
            } else it
        }
        _dsRoadmap.value = updated
    }

    fun toggleProjectTask(projectId: String, taskId: String) {
        val updated = _portfolioProjects.value.map { project ->
            if (project.id == projectId) {
                val updatedTasks = project.tasks.map {
                    if (it.id == taskId) it.copy(isDone = !it.isDone) else it
                }
                val doneCount = updatedTasks.count { it.isDone }
                val progress = if (updatedTasks.isNotEmpty()) (doneCount * 100) / updatedTasks.size else 0
                val status = when {
                    progress == 100 -> "Completed"
                    progress > 0 -> "In Progress"
                    else -> "Not Started"
                }
                project.copy(tasks = updatedTasks, progressPercent = progress, status = status)
            } else project
        }
        _portfolioProjects.value = updated
    }

    fun addInternship(app: InternshipApplication) {
        _internships.value = listOf(app) + _internships.value
    }

    fun updateInternshipStatus(id: String, status: InternshipStatus) {
        val updated = _internships.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
        _internships.value = updated
    }

    // ----------------- Health, Habits & Distraction Operations -----------------
    fun updateHealthLog(log: HealthLog) {
        _healthLog.value = log
        recalculateTodayScore()
    }

    fun toggleHabit(habitId: String) {
        val updated = _habits.value.map { habit ->
            if (habit.id == habitId) {
                val newStatus = !habit.completedToday
                val newStreak = if (newStatus) habit.currentStreak + 1 else (habit.currentStreak - 1).coerceAtLeast(0)
                val new7 = habit.last7Days.toMutableList()
                if (new7.isNotEmpty()) {
                    new7[new7.size - 1] = newStatus
                }
                habit.copy(completedToday = newStatus, currentStreak = newStreak, last7Days = new7)
            } else habit
        }
        _habits.value = updated
        recalculateTodayScore()
    }

    fun updateDistractionLog(instagram: Int, youtube: Int, gaming: Int, other: Int) {
        _distractionLog.value = _distractionLog.value.copy(
            instagramMinutes = instagram,
            youtubeMinutes = youtube,
            gamingMinutes = gaming,
            otherMinutes = other
        )
        recalculateTodayScore()
    }

    fun updateJournal(journal: DailyJournal) {
        _journal.value = journal
    }

    // ----------------- AI Chat Operations -----------------
    fun clearChatHistory() {
        _chatMessages.value = loadInitialChatMessages(_userProfile.value.name)
    }

    fun sendUserMessage(text: String) {
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = text
        )
        val currentList = _chatMessages.value + userMsg
        _chatMessages.value = currentList

        val aiResponseText = generateContextualAiResponse(text)
        val aiMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis() + 1}",
            sender = MessageSender.AI,
            text = aiResponseText.first,
            quickActionSuggestions = aiResponseText.second
        )
        _chatMessages.value = currentList + aiMsg
    }

    private fun generateContextualAiResponse(userText: String): Pair<String, List<String>> {
        val prompt = userText.lowercase()
        val name = _userProfile.value.name.ifBlank { "there" }

        return when {
            prompt.contains("cgpa") || prompt.contains("academics") || prompt.contains("grade") -> {
                Pair(
                    "Here is your academic focus strategy, $name:\n\n" +
                    "• Maintain weekly consistency on derivations & quizzes.\n" +
                    "• Complete pending assignments 24 hours before deadlines.\n\n" +
                    "What academic subject needs attention today?",
                    listOf("Open Academics", "Set Focus Timer", "Add Assignment Task")
                )
            }
            prompt.contains("career") || prompt.contains("internship") || prompt.contains("python") || prompt.contains("code") -> {
                Pair(
                    "Your career momentum plan:\n\n" +
                    "1. Focus on core technical skills (Pandas, SQL, Problem solving)\n" +
                    "2. Build 1 solid portfolio project published on GitHub\n" +
                    "3. Apply to verified internships with tailored cover notes.\n\n" +
                    "Ready to make progress on your roadmap today?",
                    listOf("View Career Roadmap", "Open Project Checklist", "Log Focus Session")
                )
            }
            prompt.contains("distract") || prompt.contains("focus") || prompt.contains("phone") || prompt.contains("procrastinat") -> {
                Pair(
                    "Distractions happen to everyone, $name. Don't worry about yesterday.\n\n" +
                    "Take one single small step right now:\n" +
                    "1. Put your phone in another room or turn on Do Not Disturb.\n" +
                    "2. Start a 25-minute Deep Focus block.\n" +
                    "3. Tackle just the first question/task.\n\n" +
                    "Shall we start the timer?",
                    listOf("Start 25 min Focus", "Log Screen Time", "View Top Priorities")
                )
            }
            else -> {
                Pair(
                    "Hello $name! I'm your NOVA AI Coach. Let's make today count.\n\n" +
                    "What would you like to focus on — Academics, Career & Projects, Routine, or a Deep Study block?",
                    listOf("What should I do today?", "Next 2 hours plan", "Start Focus Timer", "Check Priorities")
                )
            }
        }
    }

    // Genuinely calculates score from real activity done today (starts at 0)
    private fun recalculateTodayScore() {
        var score = 0
        val completedTasks = _tasks.value.count { it.isCompleted }
        score += (completedTasks * 10).coerceAtMost(30)

        val completedHabits = _habits.value.count { it.completedToday }
        score += (completedHabits * 5).coerceAtMost(25)

        if (_healthLog.value.gymCompleted) score += 15
        if (_healthLog.value.sleepHours >= 7.0) score += 10

        val focusHrs = _focusSessions.value.sumOf { it.durationMinutes } / 60.0
        if (focusHrs > 0.0) {
            score += ((focusHrs * 10).toInt()).coerceAtMost(20)
        }

        val updated = _userProfile.value.copy(todayScore = score.coerceIn(0, 100))
        saveProfile(updated)
    }

    // ----------------- Persistence Helpers -----------------
    private fun saveSession(session: AuthSession) {
        _authSession.value = session
        prefs.edit().putString("auth_session_json", json.encodeToString(session)).apply()
    }

    private fun loadStoredSession(): AuthSession {
        val raw = prefs.getString("auth_session_json", null) ?: return AuthSession()
        return try {
            json.decodeFromString<AuthSession>(raw)
        } catch (e: Exception) {
            AuthSession()
        }
    }

    private fun saveProfile(profile: UserProfile) {
        _userProfile.value = profile
        prefs.edit().putString("user_profile_json", json.encodeToString(profile)).apply()
    }

    private fun loadStoredProfile(): UserProfile {
        val raw = prefs.getString("user_profile_json", null) ?: return UserProfile()
        return try {
            json.decodeFromString<UserProfile>(raw)
        } catch (e: Exception) {
            UserProfile()
        }
    }

    private fun loadStoredSettings(): AppSettings {
        val raw = prefs.getString("app_settings_json", null) ?: return AppSettings(supabaseUrl = defaultSupabaseUrl, supabaseAnonKey = defaultSupabaseAnonKey)
        return try {
            json.decodeFromString<AppSettings>(raw)
        } catch (e: Exception) {
            AppSettings(supabaseUrl = defaultSupabaseUrl, supabaseAnonKey = defaultSupabaseAnonKey)
        }
    }

    private fun saveDegrees(list: List<DegreeInfo>) {
        prefs.edit().putString("degrees_json", json.encodeToString(list)).apply()
    }

    private fun loadStoredDegrees(): List<DegreeInfo> {
        val raw = prefs.getString("degrees_json", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<DegreeInfo>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveSubjects(list: List<Subject>) {
        prefs.edit().putString("subjects_json", json.encodeToString(list)).apply()
    }

    private fun loadStoredSubjects(): List<Subject> {
        val raw = prefs.getString("subjects_json", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<Subject>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveTasks(list: List<TaskItem>) {
        prefs.edit().putString("tasks_json", json.encodeToString(list)).apply()
    }

    private fun loadStoredTasks(): List<TaskItem> {
        val raw = prefs.getString("tasks_json", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<TaskItem>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadStoredClasses(): List<ClassSession> {
        val raw = prefs.getString("classes_json", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<ClassSession>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun generateInitialSubjectsForDegrees(degrees: List<DegreeInfo>): List<Subject> {
        return degrees.flatMap { deg ->
            listOf(
                Subject(id = "subj_${deg.type.name}_1", name = "Core Subject 1", degreeType = deg.type, credits = 4, currentScore = deg.currentCgpa * 10, targetScore = deg.targetCgpa * 10, difficulty = "Hard", status = SubjectStatus.LEARNING),
                Subject(id = "subj_${deg.type.name}_2", name = "Core Subject 2", degreeType = deg.type, credits = 4, currentScore = deg.currentCgpa * 10, targetScore = deg.targetCgpa * 10, difficulty = "Medium", status = SubjectStatus.LEARNING)
            )
        }
    }

    private fun loadInitialRoadmap() = listOf(
        RoadmapStage(stageNumber = 1, title = "Programming Fundamentals", description = "Syntax, data structures, algorithms, modular coding", progressPercent = 0, estimatedHours = 25, totalLessons = 12, totalPracticeProblems = 30),
        RoadmapStage(stageNumber = 2, title = "Data Structures & Libraries", description = "NumPy, Pandas, arrays, vectorization, indexing", progressPercent = 0, estimatedHours = 20, totalLessons = 10, totalPracticeProblems = 25),
        RoadmapStage(stageNumber = 3, title = "Data Analysis & Visualization", description = "Data cleaning, Matplotlib, Seaborn, exploratory analysis", progressPercent = 0, estimatedHours = 30, totalLessons = 14, totalPracticeProblems = 40),
        RoadmapStage(stageNumber = 4, title = "Databases & SQL", description = "Relational queries, joins, aggregations, window functions", progressPercent = 0, estimatedHours = 25, totalLessons = 12, totalPracticeProblems = 35),
        RoadmapStage(stageNumber = 5, title = "Applied Mathematics & Stats", description = "Probability, hypothesis testing, distributions", progressPercent = 0, estimatedHours = 35, totalLessons = 15, totalPracticeProblems = 45),
        RoadmapStage(stageNumber = 6, title = "Portfolio Projects", description = "End-to-end practical project published on GitHub", progressPercent = 0, estimatedHours = 30, totalLessons = 3, totalPracticeProblems = 3),
        RoadmapStage(stageNumber = 7, title = "Internship Applications", description = "Resume polishing, cold outreach, applying to roles", progressPercent = 0, estimatedHours = 20, totalLessons = 10, totalPracticeProblems = 25)
    )

    private fun loadInitialProjects() = listOf(
        PortfolioProject(
            id = "proj_1",
            title = "Personal Data & Performance Analyzer",
            description = "End-to-end exploratory analysis demonstrating clean code, visualization, and actionable insights.",
            goal = "Demonstrate practical technical skills for portfolio and internship applications.",
            techStack = listOf("Python", "Pandas", "NumPy", "Matplotlib"),
            githubUrl = "",
            status = "Not Started",
            progressPercent = 0,
            tasks = listOf(
                ProjectTask("pt_1", "Choose and clean dataset", false),
                ProjectTask("pt_2", "Perform exploratory data analysis", false),
                ProjectTask("pt_3", "Generate visual charts & correlations", false),
                ProjectTask("pt_4", "Write summary insights & GitHub README", false),
                ProjectTask("pt_5", "Publish public repository", false)
            )
        )
    )

    private fun loadInitialHabits() = listOf(
        HabitItem("h_1", "Deep Study Block", "School", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_2", "Coding / Skill Building", "Code", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_3", "Gym / Workout", "FitnessCenter", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_4", "7+ Hours Sleep", "Bedtime", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_5", "Mindful Screen Time", "TimerOff", 0, false, listOf(false, false, false, false, false, false, false))
    )

    private fun loadInitialExams() = listOf(
        ExamTrack(name = "GATE", paper = "Technical", status = "Exploration", syllabusProgress = 0, pyqCompleted = 0, totalPyqs = 500, targetYear = "2028", targetInstitutes = "IITs / IISc", isActive = false),
        ExamTrack(name = "JAM", paper = "Sciences", status = "Exploration", syllabusProgress = 0, pyqCompleted = 0, totalPyqs = 400, targetYear = "2027", targetInstitutes = "IITs", isActive = false)
    )

    private fun loadInitialChatMessages(userName: String = ""): List<ChatMessage> {
        val greetingName = if (userName.isNotBlank()) " $userName" else ""
        return listOf(
            ChatMessage(
                id = "m_0",
                sender = MessageSender.AI,
                text = "Welcome to NOVA$greetingName.\n\nI'm your personal AI Coach, connected to your goals and schedule. What would you like to accomplish today?",
                quickActionSuggestions = listOf("What should I focus on?", "Plan my next 2 hours", "Start Focus Timer", "Check Priorities")
            )
        )
    }
}
