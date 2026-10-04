package com.focusos.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.focusos.app.data.models.*
import com.focusos.app.data.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FocusOsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("focus_os_prefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Default Supabase configuration
    val defaultSupabaseUrl = "https://yvhzfrhtvwsnmzcruyyk.supabase.co"
    val defaultSupabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl2aHpmcmh0dndzbm16Y3J1eXlrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTExMTY1MTksImV4cCI6MjEwNjY5MjUxOX0.GmDiVkrVt5iYsEe7dOkaC2Wt2Z4OxJ0OxTF_YR-OduA"

    // Reactive StateFlows
    private val _userProfile = MutableStateFlow(loadInitialProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _appSettings = MutableStateFlow(loadInitialSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _degrees = MutableStateFlow(loadInitialDegrees())
    val degrees: StateFlow<List<DegreeInfo>> = _degrees.asStateFlow()

    private val _subjects = MutableStateFlow(loadInitialSubjects())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _classes = MutableStateFlow(loadInitialClasses())
    val classes: StateFlow<List<ClassSession>> = _classes.asStateFlow()

    private val _missedRecoveries = MutableStateFlow(loadInitialMissedRecoveries())
    val missedRecoveries: StateFlow<List<MissedClassRecovery>> = _missedRecoveries.asStateFlow()

    private val _tasks = MutableStateFlow(loadInitialTasks())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _focusSessions = MutableStateFlow(loadInitialFocusSessions())
    val focusSessions: StateFlow<List<FocusSession>> = _focusSessions.asStateFlow()

    private val _dsRoadmap = MutableStateFlow(loadInitialRoadmap())
    val dsRoadmap: StateFlow<List<RoadmapStage>> = _dsRoadmap.asStateFlow()

    private val _portfolioProjects = MutableStateFlow(loadInitialProjects())
    val portfolioProjects: StateFlow<List<PortfolioProject>> = _portfolioProjects.asStateFlow()

    private val _internships = MutableStateFlow(loadInitialInternships())
    val internships: StateFlow<List<InternshipApplication>> = _internships.asStateFlow()

    private val _examTracks = MutableStateFlow(loadInitialExams())
    val examTracks: StateFlow<List<ExamTrack>> = _examTracks.asStateFlow()

    private val _healthLog = MutableStateFlow(loadInitialHealthLog())
    val healthLog: StateFlow<HealthLog> = _healthLog.asStateFlow()

    private val _habits = MutableStateFlow(loadInitialHabits())
    val habits: StateFlow<List<HabitItem>> = _habits.asStateFlow()

    private val _distractionLog = MutableStateFlow(loadInitialDistractionLog())
    val distractionLog: StateFlow<DistractionLog> = _distractionLog.asStateFlow()

    private val _journal = MutableStateFlow(loadInitialJournal())
    val journal: StateFlow<DailyJournal> = _journal.asStateFlow()

    private val _chatMessages = MutableStateFlow(loadInitialChatMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _morningBrief = MutableStateFlow(MorningBrief())
    val morningBrief: StateFlow<MorningBrief> = _morningBrief.asStateFlow()

    // ----------------- Profile / Settings Operations -----------------
    fun updateProfile(profile: UserProfile) {
        _userProfile.value = profile
        recalculateTodayScore()
    }

    fun updateSettings(settings: AppSettings) {
        _appSettings.value = settings
    }

    fun completeOnboarding() {
        _userProfile.value = _userProfile.value.copy(onboardingCompleted = true)
    }

    // ----------------- Academic Operations -----------------
    fun updateDegree(type: DegreeType, currentCgpa: Double, targetCgpa: Double, completedCredits: Int) {
        val updated = _degrees.value.map {
            if (it.type == type) {
                it.copy(
                    currentCgpa = currentCgpa,
                    targetCgpa = targetCgpa,
                    completedCredits = completedCredits
                )
            } else it
        }
        _degrees.value = updated
    }

    fun updateSubjectStatus(subjectId: String, newStatus: SubjectStatus, newScore: Double? = null) {
        val updated = _subjects.value.map {
            if (it.id == subjectId) {
                it.copy(
                    status = newStatus,
                    currentScore = newScore ?: it.currentScore
                )
            } else it
        }
        _subjects.value = updated
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

            val catchUpTask = TaskItem(
                id = "task_catchup_${System.currentTimeMillis()}",
                title = "Catch up on ${session.subjectName}: ${session.topic}",
                category = if (session.degreeType == DegreeType.IITM) TaskCategory.IITM else TaskCategory.ITEP,
                priority = TaskPriority.HIGH,
                estimatedMinutes = 45,
                deadline = "Next 48h",
                isCatchUpTask = true,
                relatedSubject = session.subjectName
            )
            _tasks.value = listOf(catchUpTask) + _tasks.value
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
        _tasks.value = listOf(task) + _tasks.value
        recalculateTodayScore()
    }

    fun toggleTask(taskId: String) {
        val updated = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        _tasks.value = updated
        recalculateTodayScore()
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        recalculateTodayScore()
    }

    // ----------------- Focus Operations -----------------
    fun logFocusSession(session: FocusSession) {
        _focusSessions.value = listOf(session) + _focusSessions.value
        val addedHours = session.durationMinutes / 60.0
        val currentProfile = _userProfile.value
        _userProfile.value = currentProfile.copy(
            totalFocusedHoursThisWeek = currentProfile.totalFocusedHoursThisWeek + addedHours
        )
        recalculateTodayScore()
    }

    // ----------------- Career / Roadmap Operations -----------------
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

    // ----------------- Health & Habit Operations -----------------
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

    // ----------------- Journal Operations -----------------
    fun updateJournal(journal: DailyJournal) {
        _journal.value = journal
    }

    // ----------------- AI Chat Operations -----------------
    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = "m_0",
                sender = MessageSender.AI,
                text = "Chat history refreshed. Main aapka Focus OS AI Mentor hoon. Aaj kya plan karein?",
                quickActionSuggestions = listOf("Bhai aaj kya karu?", "Kal mera IITM assignment hai", "Meri CGPA kaise improve hogi?", "Next 2 hours plan")
            )
        )
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
        val itepCgpa = _degrees.value.find { it.type == DegreeType.ITEP }?.currentCgpa ?: 6.8
        val iitmCgpa = _degrees.value.find { it.type == DegreeType.IITM }?.currentCgpa ?: 5.5
        val missedCount = _missedRecoveries.value.count { !it.isRecovered }

        return when {
            prompt.contains("cgpa") || prompt.contains("itep") || prompt.contains("iitm") || prompt.contains("marks") -> {
                Pair(
                    "Ravi, yahan hai aapka clear CGPA recovery roadmap:\n\n" +
                    "• **ITEP (Maths) — Current: $itepCgpa**\n" +
                    "  Target 7.5 ke liye aapko agle semesters mein average **8.12 SGPA** lana hai. Real Analysis aur Abstract Algebra mein derivations par dhyan do.\n\n" +
                    "• **IITM (Data Science) — Current: $iitmCgpa**\n" +
                    "  Target 6.5+ ke liye **~6.7 SGPA** chahiye. Weekly quizzes aur assignment deadlines miss mat karo.\n\n" +
                    "✅ *Immediate Action:* Week 4 Statistics assignment kal submission se pehle complete kar lo.",
                    listOf("Open Academics", "Calculate SGPA", "Check Subject Status")
                )
            }
            prompt.contains("gate") || prompt.contains("jam") || prompt.contains("ssc") || prompt.contains("career") -> {
                Pair(
                    "Mera honest advice Ravi: **Abhi simultaneously GATE + JAM + SSC start mat karo.**\n\n" +
                    "Aap pehle se 2 heavy degrees kar rahe ho (ITEP Math + IITM DS) aur aapka agla milestone **₹5k-6k/mo paid internship** hai.\n\n" +
                    "🎯 **Focus Strategy:**\n" +
                    "1. 70% Energy → ITEP & IITM CGPA Recovery\n" +
                    "2. 30% Energy → Python + Pandas + Mini Project\n" +
                    "3. GATE/JAM → Sirf syllabus dekho, exam season mein decide karenge.",
                    listOf("Check DS Roadmap", "View Mini Project", "View Internships")
                )
            }
            prompt.contains("distracted") || prompt.contains("focus") || prompt.contains("procrastinat") || prompt.contains("phone") || prompt.contains("bhatak") -> {
                Pair(
                    "Bhai, har student distract hota hai, guilt feel karne ki bilkul zaroorat nahi hai.\n\n" +
                    "Pura din plan karne ki koshish mat karo. Bas **ek tiny step** lo:\n\n" +
                    "1. Phone ko bed ya dusre room mein rakh do.\n" +
                    "2. 25-minute ka Focus Timer start karo.\n" +
                    "3. Bas assignment ka Question #1 kholo.\n\n" +
                    "Jaise hi 5 minute nikalenge, momentum apne aap ban jayega.",
                    listOf("Start 25 min Focus", "Log Screen Time", "Check Today Top 3")
                )
            }
            prompt.contains("python") || prompt.contains("data science") || prompt.contains("project") || prompt.contains("pandas") || prompt.contains("sql") -> {
                Pair(
                    "Data Science foundation track:\n\n" +
                    "• **Stage 1 (Python Basics):** Start with fundamentals\n" +
                    "• **Stage 2 (NumPy & Math):** Arrays & vector operations\n" +
                    "• **Stage 3 (Pandas):** Data cleaning & analysis\n\n" +
                    "📁 **Student Performance Analyzer Project:**\n" +
                    "Step 1 hai Dataset load karna aur EDA script tayyar karna. Checklist se ek task complete karo.",
                    listOf("Open Project Checklist", "DS Roadmap 9 Stages", "Log Coding Habit")
                )
            }
            prompt.contains("schedule") || prompt.contains("assignment") || prompt.contains("kal") || prompt.contains("timetable") -> {
                Pair(
                    "Ye raha aapke liye optimized schedule:\n\n" +
                    "• **Block 1 (02:30 PM - 04:00 PM):** 🧠 Deep Study — IITM Statistics\n" +
                    "• **Block 2 (04:30 PM - 05:45 PM):** 📐 ITEP Real Analysis derivations\n" +
                    "• **Block 3 (07:00 PM - 08:00 PM):** 🏋️‍♂️ Gym / Workout\n" +
                    "• **Block 4 (09:00 PM - 10:00 PM):** 💻 Coding / Project Practice\n" +
                    "• **Block 5 (10:15 PM):** 🌙 Evening Check-in & sleep by 11:00 PM",
                    listOf("Start Block 1 Focus", "Set Reminder", "View Timetable")
                )
            }
            prompt.contains("next 2 hours") || prompt.contains("2 hours") || prompt.contains("agle 2 ghante") -> {
                Pair(
                    "Agle 2 hours ka direct action plan:\n\n" +
                    "⏱️ **First 50 mins:** IITM Statistics Assignment Questions 1 to 4 solve karo.\n" +
                    "☕ **10 mins Break:** Paani piyo, screen se door dekho.\n" +
                    "⏱️ **Next 50 mins:** Real Analysis ke 2 main theorem proofs paper par likho.\n\n" +
                    "Timer start karne ke liye ready ho?",
                    listOf("Start 50m Focus Session", "View Top 3 Tasks")
                )
            }
            else -> {
                Pair(
                    "Samajh gaya Ravi. Aaj ka din fresh start hai! Aapko kis specific cheez mein help chahiye — Schedule, CGPA strategy, Data Science project, ya Focus session?",
                    listOf("Bhai aaj kya karu?", "Meri CGPA kaise improve hogi?", "Next 2 hours plan", "Start Focus Timer")
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

        _userProfile.value = _userProfile.value.copy(todayScore = score.coerceIn(0, 100))
    }

    private fun loadInitialProfile() = UserProfile(
        id = "user_ravi_1",
        name = "Ravi",
        email = "ravi@focusos.app",
        onboardingCompleted = true,
        majorGoal1 = "CGPA Recovery (ITEP -> 7.5+, IITM -> 6.5+)",
        majorGoal2 = "Data Science Foundation & Paid Internship (₹5k-6k/mo)",
        totalFocusedHoursThisWeek = 0.0,
        currentStreakDays = 0,
        todayScore = 0
    )

    private fun loadInitialSettings() = AppSettings(
        themeMode = "dark",
        dailyMorningBriefTime = "07:30 AM",
        dailyEveningCheckinTime = "09:30 PM",
        maxDailyNotifications = 4,
        notificationsEnabled = true,
        supabaseUrl = defaultSupabaseUrl,
        supabaseAnonKey = defaultSupabaseAnonKey
    )

    private fun loadInitialDegrees() = listOf(
        DegreeInfo(
            type = DegreeType.ITEP,
            name = "ITEP — B.Sc. B.Ed. Mathematics",
            currentYear = 2,
            totalYears = 4,
            currentCgpa = 6.8,
            targetCgpa = 7.5,
            completedCredits = 44,
            totalCredits = 96,
            currentSemester = 3,
            isPrimaryFocus = true
        ),
        DegreeInfo(
            type = DegreeType.IITM,
            name = "IIT Madras — BS Data Science",
            currentYear = 1,
            totalYears = 4,
            currentCgpa = 5.5,
            targetCgpa = 6.5,
            completedCredits = 20,
            totalCredits = 116,
            currentSemester = 2,
            isPrimaryFocus = true
        )
    )

    private fun loadInitialSubjects() = listOf(
        Subject(
            id = "subj_itep_1",
            name = "Real Analysis",
            degreeType = DegreeType.ITEP,
            credits = 4,
            currentScore = 70.0,
            targetScore = 85.0,
            difficulty = "Hard",
            status = SubjectStatus.NEEDS_REVISION
        ),
        Subject(
            id = "subj_itep_2",
            name = "Abstract Algebra",
            degreeType = DegreeType.ITEP,
            credits = 4,
            currentScore = 74.0,
            targetScore = 80.0,
            difficulty = "Hard",
            status = SubjectStatus.NEEDS_REVISION
        ),
        Subject(
            id = "subj_itep_3",
            name = "Educational Psychology",
            degreeType = DegreeType.ITEP,
            credits = 3,
            currentScore = 82.0,
            targetScore = 85.0,
            difficulty = "Medium",
            status = SubjectStatus.LEARNING
        ),
        Subject(
            id = "subj_iitm_1",
            name = "Statistics for Data Science I",
            degreeType = DegreeType.IITM,
            credits = 4,
            currentScore = 58.0,
            targetScore = 75.0,
            difficulty = "Hard",
            status = SubjectStatus.NEEDS_REVISION
        ),
        Subject(
            id = "subj_iitm_2",
            name = "Mathematics for Data Science I",
            degreeType = DegreeType.IITM,
            credits = 4,
            currentScore = 62.0,
            targetScore = 75.0,
            difficulty = "Hard",
            status = SubjectStatus.NEEDS_REVISION
        ),
        Subject(
            id = "subj_iitm_3",
            name = "Computational Thinking (Python)",
            degreeType = DegreeType.IITM,
            credits = 4,
            currentScore = 78.0,
            targetScore = 85.0,
            difficulty = "Medium",
            status = SubjectStatus.LEARNING
        )
    )

    private fun loadInitialClasses() = listOf(
        ClassSession(
            id = "cls_1",
            subjectName = "IITM — Statistics I",
            degreeType = DegreeType.IITM,
            timeSlot = "10:00 AM - 11:30 AM",
            topic = "Bayes Theorem & Conditional Probability",
            status = ClassAttendanceStatus.UPCOMING,
            date = "Today"
        ),
        ClassSession(
            id = "cls_2",
            subjectName = "ITEP — Real Analysis",
            degreeType = DegreeType.ITEP,
            timeSlot = "12:00 PM - 01:00 PM",
            topic = "Cauchy Sequences & Convergence",
            status = ClassAttendanceStatus.UPCOMING,
            date = "Today"
        ),
        ClassSession(
            id = "cls_3",
            subjectName = "ITEP — Educational Psychology",
            degreeType = DegreeType.ITEP,
            timeSlot = "02:00 PM - 03:00 PM",
            topic = "Cognitive Development Stages (Piaget)",
            status = ClassAttendanceStatus.UPCOMING,
            date = "Today"
        )
    )

    private fun loadInitialMissedRecoveries() = listOf(
        MissedClassRecovery(
            id = "rec_1",
            classSessionId = "cls_old_1",
            subjectName = "IITM — Mathematics for DS",
            degreeType = DegreeType.IITM,
            topic = "Linear Algebra Matrix Transformations",
            missedDate = "Yesterday",
            watchLectureDone = false,
            notesDone = false,
            quizDone = false,
            revisionDone = false,
            isRecovered = false
        )
    )

    private fun loadInitialTasks() = listOf(
        TaskItem(
            id = "task_1",
            title = "Solve IITM Statistics Week 4 Assignment (Q1-Q10)",
            category = TaskCategory.IITM,
            priority = TaskPriority.HIGH,
            estimatedMinutes = 60,
            deadline = "Today 08:00 PM",
            isTop3 = true,
            isCompleted = false,
            relatedSubject = "Statistics for Data Science I"
        ),
        TaskItem(
            id = "task_2",
            title = "ITEP Real Analysis sequence convergence theorem proof",
            category = TaskCategory.ITEP,
            priority = TaskPriority.HIGH,
            estimatedMinutes = 45,
            deadline = "Today 10:00 PM",
            isTop3 = true,
            isCompleted = false,
            relatedSubject = "Real Analysis"
        ),
        TaskItem(
            id = "task_3",
            title = "Data Science: Load dataset into Pandas DataFrame",
            category = TaskCategory.DATA_SCIENCE,
            priority = TaskPriority.HIGH,
            estimatedMinutes = 40,
            deadline = "Tonight",
            isTop3 = true,
            isCompleted = false
        ),
        TaskItem(
            id = "task_4",
            title = "Gym: Pull Day (Back + Biceps) 45 min workout",
            category = TaskCategory.HEALTH,
            priority = TaskPriority.MEDIUM,
            estimatedMinutes = 45,
            deadline = "07:00 PM",
            isCompleted = false
        ),
        TaskItem(
            id = "task_5",
            title = "Apply to 2 remote Data Analyst Internships on Internshala",
            category = TaskCategory.INTERNSHIP,
            priority = TaskPriority.MEDIUM,
            estimatedMinutes = 30,
            deadline = "Tomorrow",
            isCompleted = false
        )
    )

    private fun loadInitialFocusSessions(): List<FocusSession> = emptyList()

    // Clean initial Roadmap without fake progress (starts at 0%)
    private fun loadInitialRoadmap() = listOf(
        RoadmapStage(
            stageNumber = 1,
            title = "Python Fundamentals",
            description = "Variables, loops, functions, OOP, list comprehensions, modules",
            progressPercent = 0,
            estimatedHours = 25,
            lessonsCompleted = 0,
            totalLessons = 12,
            practiceProblemsDone = 0,
            totalPracticeProblems = 30,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 2,
            title = "NumPy & Numerical Computing",
            description = "N-d arrays, vectorization, broadcasting, matrix algebra",
            progressPercent = 0,
            estimatedHours = 15,
            lessonsCompleted = 0,
            totalLessons = 8,
            practiceProblemsDone = 0,
            totalPracticeProblems = 20,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 3,
            title = "Pandas Data Manipulation",
            description = "DataFrames, filtering, group by, merging, handling nulls, transformations",
            progressPercent = 0,
            estimatedHours = 30,
            lessonsCompleted = 0,
            totalLessons = 14,
            practiceProblemsDone = 0,
            totalPracticeProblems = 40,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 4,
            title = "Data Visualization",
            description = "Matplotlib, Seaborn: distributions, heatmaps, categorical plots, storytelling",
            progressPercent = 0,
            estimatedHours = 20,
            lessonsCompleted = 0,
            totalLessons = 10,
            practiceProblemsDone = 0,
            totalPracticeProblems = 25,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 5,
            title = "Exploratory Data Analysis (EDA)",
            description = "End-to-end dataset cleaning, anomaly detection, statistical summaries",
            progressPercent = 0,
            estimatedHours = 25,
            lessonsCompleted = 0,
            totalLessons = 8,
            practiceProblemsDone = 0,
            totalPracticeProblems = 15,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 6,
            title = "SQL & Relational Databases",
            description = "SELECT, JOINs, aggregations, window functions, CTEs with SQLite/PostgreSQL",
            progressPercent = 0,
            estimatedHours = 25,
            lessonsCompleted = 0,
            totalLessons = 12,
            practiceProblemsDone = 0,
            totalPracticeProblems = 35,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 7,
            title = "Mathematics & Applied Statistics",
            description = "Probability distributions, hypothesis testing, p-values, Bayes theorem",
            progressPercent = 0,
            estimatedHours = 35,
            lessonsCompleted = 0,
            totalLessons = 15,
            practiceProblemsDone = 0,
            totalPracticeProblems = 45,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 8,
            title = "Portfolio Projects",
            description = "Real-world dataset end-to-end analysis published on GitHub",
            progressPercent = 0,
            estimatedHours = 30,
            lessonsCompleted = 0,
            totalLessons = 3,
            practiceProblemsDone = 0,
            totalPracticeProblems = 3,
            isCompleted = false
        ),
        RoadmapStage(
            stageNumber = 9,
            title = "Internship Applications",
            description = "Resume polishing, cold outreach, applying for ₹5k-6k/month roles",
            progressPercent = 0,
            estimatedHours = 20,
            lessonsCompleted = 0,
            totalLessons = 10,
            practiceProblemsDone = 0,
            totalPracticeProblems = 25,
            isCompleted = false
        )
    )

    // Clean project with 0% progress
    private fun loadInitialProjects() = listOf(
        PortfolioProject(
            id = "proj_1",
            title = "Student Performance Analyzer",
            description = "End-to-end Python exploratory data analysis examining study hours, attendance, and CGPA correlation with actionable insights.",
            goal = "Demonstrate practical data analysis & visualization skills for internship applications.",
            techStack = listOf("Python", "Pandas", "NumPy", "Matplotlib", "Seaborn"),
            githubUrl = "https://github.com/ravi/student-performance-analyzer",
            status = "Not Started",
            progressPercent = 0,
            tasks = listOf(
                ProjectTask("pt_1", "Find dataset on Kaggle/UCI", false),
                ProjectTask("pt_2", "Load dataset into Pandas", false),
                ProjectTask("pt_3", "Inspect dataset info & summary stats", false),
                ProjectTask("pt_4", "Clean data & handle missing values", false),
                ProjectTask("pt_5", "Analyze score averages by group", false),
                ProjectTask("pt_6", "Analyze study hours vs performance", false),
                ProjectTask("pt_7", "Analyze attendance correlation", false),
                ProjectTask("pt_8", "Analyze previous term scores", false),
                ProjectTask("pt_9", "Calculate correlation matrix heatmap", false),
                ProjectTask("pt_10", "Create visual charts & subplots", false),
                ProjectTask("pt_11", "Write data insights summary", false),
                ProjectTask("pt_12", "Create polished GitHub README", false),
                ProjectTask("pt_13", "Publish public GitHub repository", false)
            )
        )
    )

    private fun loadInitialInternships() = listOf(
        InternshipApplication(
            id = "intern_1",
            company = "EduTech Insights",
            role = "Data Analyst Intern",
            stipend = "₹6,000/month",
            location = "Remote",
            appliedDate = "2026-10-04",
            followUpDate = "2026-10-11",
            status = InternshipStatus.APPLIED,
            notes = "Target position for practical Pandas & visualization experience."
        )
    )

    // Clean Exam tracks without dummy progress
    private fun loadInitialExams() = listOf(
        ExamTrack(
            name = "GATE",
            paper = "Mathematics (MA)",
            status = "Future / Exploration",
            syllabusProgress = 0,
            pyqCompleted = 0,
            totalPyqs = 500,
            targetYear = "2028",
            targetInstitutes = "IITs / IISc",
            isActive = false
        ),
        ExamTrack(
            name = "JAM",
            paper = "Mathematics (MA)",
            status = "Exploration",
            syllabusProgress = 0,
            pyqCompleted = 0,
            totalPyqs = 400,
            targetYear = "2027",
            targetInstitutes = "IITs for M.Sc.",
            isActive = false
        ),
        ExamTrack(
            name = "SSC",
            paper = "CGL / CHSL",
            status = "Future Option",
            syllabusProgress = 0,
            pyqCompleted = 0,
            totalPyqs = 1000,
            targetYear = "2028+",
            targetInstitutes = "Government Services",
            isActive = false
        )
    )

    // Clean Health log without fake completed values
    private fun loadInitialHealthLog() = HealthLog(
        date = "Today",
        gymCompleted = false,
        workoutType = WorkoutType.REST,
        workoutDurationMinutes = 0,
        steps = 0,
        sleepHours = 0.0,
        waterGlasses = 0,
        moodRating = 0,
        energyRating = 0
    )

    // Clean Habit tracking (starts with 0 streak and false today)
    private fun loadInitialHabits() = listOf(
        HabitItem("h_1", "Study (Academics)", "School", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_2", "Coding / Data Science", "Code", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_3", "Gym / Physical Workout", "FitnessCenter", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_4", "7+ Hours Sleep", "Bedtime", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_5", "Reading Math Concepts", "MenuBook", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_6", "No Mindless Scrolling", "TimerOff", 0, false, listOf(false, false, false, false, false, false, false)),
        HabitItem("h_7", "Daily Evening Review", "RateReview", 0, false, listOf(false, false, false, false, false, false, false))
    )

    private fun loadInitialDistractionLog() = DistractionLog(
        date = "Today",
        instagramMinutes = 0,
        youtubeMinutes = 0,
        gamingMinutes = 0,
        otherMinutes = 0,
        yesterdayTotalMinutes = 0
    )

    private fun loadInitialJournal() = DailyJournal(
        id = "j_today",
        date = "Today",
        whatWentWell = "",
        whatWentWrong = "",
        whatToImproveTomorrow = "",
        moodRating = 0,
        energyRating = 0,
        aiPatternInsight = "Log your daily progress and focus sessions to generate personalized insights."
    )

    private fun loadInitialChatMessages() = listOf(
        ChatMessage(
            id = "m_0",
            sender = MessageSender.AI,
            text = "Good day Ravi! Main aapka Focus OS AI Mentor hoon.\n\nAapki primary priorities:\n1. 🎯 **CGPA Recovery** (ITEP 6.8 & IITM 5.5)\n2. 📊 **Data Science & Paid Internship** (₹5k-6k/mo target)\n\nBatao bhai, aaj kis cheez par kaam start karein?",
            quickActionSuggestions = listOf("Bhai aaj kya karu?", "Kal mera IITM assignment hai", "Meri CGPA kaise improve hogi?", "Next 2 hours plan")
        )
    )
}
