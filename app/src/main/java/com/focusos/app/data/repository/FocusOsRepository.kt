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

    fun markClassStatus(classId: String, status: ClassAttendanceStatus) {
        val list = _classes.value.toMutableList()
        val index = list.indexOfFirst { it.id == classId }
        if (index != -1) {
            val session = list[index]
            val isMissed = (status == ClassAttendanceStatus.ABSENT)
            val updatedSession = session.copy(status = status, isMissed = isMissed)
            list[index] = updatedSession
            _classes.value = list

            if (isMissed) {
                createMissedClassRecovery(updatedSession)
            }
            recalculateTodayScore()
        }
    }

    private fun createMissedClassRecovery(session: ClassSession) {
        val existing = _missedRecoveries.value.find { it.classSessionId == session.id }
        if (existing == null) {
            val recovery = MissedClassRecovery(
                id = "recovery_${System.currentTimeMillis()}",
                classSessionId = session.id,
                subjectName = session.subjectName,
                degreeType = session.degreeType,
                topic = session.topic,
                missedDate = "Today"
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
                    progressPercent = newProgress,
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
                text = "Chat history refreshed. I am connected with your Supabase backend and active context. Kya plan karein?",
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

    private fun generateContextualAiResponse(userPrompt: String): Pair<String, List<String>> {
        val prompt = userPrompt.lowercase().trim()
        val itepCgpa = _degrees.value.find { it.type == DegreeType.ITEP }?.currentCgpa ?: 6.8
        val iitmCgpa = _degrees.value.find { it.type == DegreeType.IITM }?.currentCgpa ?: 5.5
        val pendingTop3 = _tasks.value.filter { it.isTop3 && !it.isCompleted }
        val missedCount = _missedRecoveries.value.count { !it.isRecovered }

        return when {
            prompt.contains("aaj kya karu") || prompt.contains("kya karu") || prompt.contains("what should i do") || prompt.contains("today plan") || prompt.contains("aaj ka plan") -> {
                val topTask = pendingTop3.firstOrNull()?.title ?: "Complete IITM Statistics assignment (Week 4)"
                Pair(
                    "Hey Ravi! Bilkul simple aur clear plan banate hain taaki koi stress na ho:\n\n" +
                    "1. 🎯 **First Priority:** $topTask\n" +
                    "   ↳ *Action:* Abhi 50-minute ka focus timer lagao aur start karo.\n\n" +
                    "2. 📚 **ITEP Math Revision:** Real Analysis theorem proofs (45 min).\n\n" +
                    "3. 🏋️‍♂️ **Health:** 45 min Gym workout (Pull day) energy maintain karne ke liye.\n\n" +
                    "💡 *Rule of Focus OS:* GATE aur SSC ko abhi side rakho. Pehle CGPA recovery + Statistics assignment finish karo.",
                    listOf("Start 50m Focus Timer", "Open IITM Assignment", "View Today Tasks")
                )
            }
            prompt.contains("cgpa") || prompt.contains("improve") || prompt.contains("marks") || prompt.contains("score") -> {
                Pair(
                    "Ravi, academic reality check aur exact math yeh hai:\n\n" +
                    "• **ITEP (Math) — Current: $itepCgpa**\n" +
                    "  Target 7.5 ke liye aapko agle semesters mein average **8.12 SGPA** lana hai. Real Analysis aur Abstract Algebra mein assignments aur derivations par dhyan do.\n\n" +
                    "• **IITM (Data Science) — Current: $iitmCgpa**\n" +
                    "  Target 6.5+ ke liye **~6.7 SGPA** chahiye. Weekly quizzes aur assignment deadlines miss mat karo — yeh single habit aapka CGPA 1.0 point bada degi.\n\n" +
                    "✅ *Immediate Action:* Week 4 Statistics assignment kal submission se pehle complete kar lo.",
                    listOf("Open Academics", "Calculate SGPA", "Check Subject Status")
                )
            }
            prompt.contains("gate") || prompt.contains("jam") || prompt.contains("ssc") || prompt.contains("career") -> {
                Pair(
                    "Mera honest advice Ravi: **Abhi simultaneously GATE + JAM + SSC start mat karo.**\n\n" +
                    "Aap pehle se 2 heavy degrees kar rahe ho (ITEP Math + IITM DS) aur aapka agla milestone **₹5k-6k/mo paid internship** hai.\n\n" +
                    "Agar aap 4 alag direction mein bhagoge toh CGPA drop hoga.\n\n" +
                    "🎯 **Focus Strategy:**\n" +
                    "1. 70% Energy → ITEP & IITM CGPA Recovery\n" +
                    "2. 30% Energy → Pandas + SQL + Mini Project (Student Performance Analyzer)\n" +
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
                    "Jaise hi 5 minute nikalenge, momentum apne aap ban jayega. Chalein start karein?",
                    listOf("Start 25 min Focus", "Log Screen Time", "Check Today Top 3")
                )
            }
            prompt.contains("python") || prompt.contains("data science") || prompt.contains("project") || prompt.contains("pandas") || prompt.contains("sql") -> {
                Pair(
                    "Aapka Data Science roadmap progress mast chal raha hai:\n\n" +
                    "• **Stage 1 (Python):** 100% Completed ✅\n" +
                    "• **Stage 2 (NumPy):** 100% Completed ✅\n" +
                    "• **Stage 3 (Pandas):** 65% (In Progress) ⏳\n\n" +
                    "📁 **Student Performance Analyzer Project:**\n" +
                    "Aapne Data Loading aur Stats summary kar liya hai. Next step hai **Missing Values Clean karna** aur **Attendance vs Performance correlation** calculate karna.",
                    listOf("Open Project Checklist", "DS Roadmap 9 Stages", "Log Coding Habit")
                )
            }
            prompt.contains("schedule") || prompt.contains("assignment") || prompt.contains("kal") || prompt.contains("timetable") -> {
                Pair(
                    "Ye raha aapke liye optimized schedule:\n\n" +
                    "• **Block 1 (02:30 PM - 04:00 PM):** 🧠 Deep Study — IITM Statistics Bayes Theorem\n" +
                    "• **Block 2 (04:30 PM - 05:45 PM):** 📐 ITEP Real Analysis — Sequence & Series derivations\n" +
                    "• **Block 3 (07:00 PM - 08:00 PM):** 🏋️‍♂️ Gym / Pull Workout (Energy booster)\n" +
                    "• **Block 4 (09:00 PM - 10:00 PM):** 💻 Pandas Data Cleaning on Project\n" +
                    "• **Block 5 (10:15 PM):** 🌙 5 min Evening Check-in & sleep by 11:00 PM",
                    listOf("Start Block 1 Focus", "Set Reminder", "View Timetable")
                )
            }
            prompt.contains("next 2 hours") || prompt.contains("2 hours") || prompt.contains("agle 2 ghante") -> {
                Pair(
                    "Agle 2 hours ka direct action plan:\n\n" +
                    "⏱️ **First 50 mins:** IITM Statistics Assignment draft Questions 1 to 4 solve karo.\n" +
                    "☕ **10 mins Break:** Paani piyo, screen se door dekho.\n" +
                    "⏱️ **Next 50 mins:** Real Analysis ke 2 main theorem proofs paper par likho.\n\n" +
                    "Timer start karne ke liye ready ho?",
                    listOf("Start 50m Focus Session", "View Top 3 Tasks")
                )
            }
            else -> {
                Pair(
                    "Samajh gaya Ravi. Aapke dashboard ke hisaab se: IITM Stats assignment due hai, $missedCount missed class recovery pending hai, aur aapka streak 7 days ka hai.\n\n" +
                    "Aapko kis specific cheez mein help chahiye — Schedule, CGPA strategy, Data Science project, ya Focus session?",
                    listOf("Bhai aaj kya karu?", "Meri CGPA kaise improve hogi?", "Next 2 hours plan", "Start Focus Timer")
                )
            }
        }
    }

    private fun recalculateTodayScore() {
        var score = 50
        val completedTasks = _tasks.value.count { it.isCompleted }
        score += (completedTasks * 6).coerceAtMost(24)

        val completedHabits = _habits.value.count { it.completedToday }
        score += (completedHabits * 3).coerceAtMost(15)

        if (_healthLog.value.gymCompleted) score += 6
        if (_healthLog.value.sleepHours >= 7.0) score += 5

        val focusHrs = _focusSessions.value.sumOf { it.durationMinutes } / 60.0
        if (focusHrs >= 2.0) score += 10

        if (_distractionLog.value.isImprovement) score += 5

        _userProfile.value = _userProfile.value.copy(todayScore = score.coerceIn(0, 100))
    }

    private fun loadInitialProfile() = UserProfile(
        id = "user_ravi_1",
        name = "Ravi",
        email = "ravi@focusos.app",
        onboardingCompleted = true,
        majorGoal1 = "CGPA Recovery (ITEP -> 7.5+, IITM -> 6.5+)",
        majorGoal2 = "Data Science Foundation & Paid Internship (₹5k-6k/mo)",
        totalFocusedHoursThisWeek = 14.5,
        currentStreakDays = 7,
        todayScore = 72
    )

    private fun loadInitialSettings() = AppSettings(
        themeMode = "system",
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
            progressPercent = 60,
            status = SubjectStatus.NEEDS_REVISION,
            nextExamDate = "Nov 12",
            nextAssignmentDate = "Oct 08",
            attendancePercent = 88
        ),
        Subject(
            id = "subj_itep_2",
            name = "Abstract Algebra",
            degreeType = DegreeType.ITEP,
            credits = 4,
            currentScore = 68.0,
            targetScore = 80.0,
            difficulty = "Medium",
            progressPercent = 50,
            status = SubjectStatus.LEARNING,
            nextExamDate = "Nov 15",
            attendancePercent = 85
        ),
        Subject(
            id = "subj_itep_3",
            name = "Pedagogy of Mathematics",
            degreeType = DegreeType.ITEP,
            credits = 3,
            currentScore = 78.0,
            targetScore = 85.0,
            difficulty = "Easy",
            progressPercent = 75,
            status = SubjectStatus.EXAM_READY,
            attendancePercent = 92
        ),
        Subject(
            id = "subj_iitm_1",
            name = "Statistics for Data Science 1",
            degreeType = DegreeType.IITM,
            credits = 4,
            currentScore = 58.0,
            targetScore = 80.0,
            difficulty = "Hard",
            progressPercent = 40,
            status = SubjectStatus.NEEDS_REVISION,
            nextAssignmentDate = "Tomorrow (11:59 PM)",
            nextExamDate = "Nov 20",
            attendancePercent = 75
        ),
        Subject(
            id = "subj_iitm_2",
            name = "Computational Thinking",
            degreeType = DegreeType.IITM,
            credits = 4,
            currentScore = 62.0,
            targetScore = 78.0,
            difficulty = "Medium",
            progressPercent = 55,
            status = SubjectStatus.LEARNING,
            nextExamDate = "Nov 22",
            attendancePercent = 80
        ),
        Subject(
            id = "subj_iitm_3",
            name = "Mathematics for Data Science 1",
            degreeType = DegreeType.IITM,
            credits = 4,
            currentScore = 54.0,
            targetScore = 75.0,
            difficulty = "Hard",
            progressPercent = 38,
            status = SubjectStatus.LEARNING,
            attendancePercent = 70
        )
    )

    private fun loadInitialClasses() = listOf(
        ClassSession(
            id = "cls_1",
            subjectName = "IITM Statistics 1 (Week 4)",
            degreeType = DegreeType.IITM,
            timeSlot = "09:00 AM – 10:30 AM",
            topic = "Probability Distributions & Bayes Theorem",
            status = ClassAttendanceStatus.ABSENT,
            isMissed = true
        ),
        ClassSession(
            id = "cls_2",
            subjectName = "ITEP Real Analysis",
            degreeType = DegreeType.ITEP,
            timeSlot = "11:00 AM – 12:30 PM",
            topic = "Sequences, Convergence & Cauchy Criterion",
            status = ClassAttendanceStatus.PRESENT
        ),
        ClassSession(
            id = "cls_3",
            subjectName = "ITEP Abstract Algebra",
            degreeType = DegreeType.ITEP,
            timeSlot = "02:00 PM – 03:15 PM",
            topic = "Cosets, Lagrange's Theorem & Normal Subgroups",
            status = ClassAttendanceStatus.WATCHED_RECORDING
        ),
        ClassSession(
            id = "cls_4",
            subjectName = "IITM Computational Thinking",
            degreeType = DegreeType.IITM,
            timeSlot = "04:00 PM – 05:30 PM",
            topic = "Recursion & Iterative Problem Solving",
            status = ClassAttendanceStatus.UPCOMING
        )
    )

    private fun loadInitialMissedRecoveries() = listOf(
        MissedClassRecovery(
            id = "rec_1",
            classSessionId = "cls_1",
            subjectName = "IITM Statistics 1 (Week 4)",
            degreeType = DegreeType.IITM,
            topic = "Probability Distributions & Bayes Theorem",
            missedDate = "Yesterday",
            watchLectureDone = true,
            notesDone = false,
            quizDone = false,
            revisionDone = false
        )
    )

    private fun loadInitialTasks() = listOf(
        TaskItem(
            id = "t_1",
            title = "Complete IITM Statistics assignment (Week 4)",
            category = TaskCategory.IITM,
            priority = TaskPriority.HIGH,
            estimatedMinutes = 50,
            deadline = "Tomorrow 11:59 PM",
            isTop3 = true
        ),
        TaskItem(
            id = "t_2",
            title = "Revise ITEP Mathematics — Real Analysis proofs",
            category = TaskCategory.ITEP,
            priority = TaskPriority.HIGH,
            estimatedMinutes = 45,
            deadline = "Today",
            isTop3 = true
        ),
        TaskItem(
            id = "t_3",
            title = "45 min Gym workout (Pull day & Core)",
            category = TaskCategory.HEALTH,
            priority = TaskPriority.MEDIUM,
            estimatedMinutes = 45,
            deadline = "Today",
            isTop3 = true,
            isCompleted = true
        ),
        TaskItem(
            id = "t_4",
            title = "Catch up: Complete notes for IITM Statistics Week 4",
            category = TaskCategory.IITM,
            priority = TaskPriority.HIGH,
            estimatedMinutes = 35,
            deadline = "Next 48h",
            isCatchUpTask = true
        ),
        TaskItem(
            id = "t_5",
            title = "Pandas practice on Student Performance dataset",
            category = TaskCategory.DATA_SCIENCE,
            priority = TaskPriority.MEDIUM,
            estimatedMinutes = 45,
            deadline = "This Weekend"
        ),
        TaskItem(
            id = "t_6",
            title = "Update GitHub README with project analysis",
            category = TaskCategory.PROJECT,
            priority = TaskPriority.LOW,
            estimatedMinutes = 25,
            deadline = "Sunday"
        )
    )

    private fun loadInitialFocusSessions() = listOf(
        FocusSession(
            id = "fs_1",
            timestamp = System.currentTimeMillis() - 7200000,
            durationMinutes = 50,
            taskTitle = "IITM Statistics Problem Set",
            category = TaskCategory.IITM,
            accomplishmentNotes = "Solved 6 Bayes theorem problems and drafted assignment"
        ),
        FocusSession(
            id = "fs_2",
            timestamp = System.currentTimeMillis() - 18000000,
            durationMinutes = 25,
            taskTitle = "Real Analysis Theorems",
            category = TaskCategory.ITEP,
            accomplishmentNotes = "Wrote proof for Bolzano-Weierstrass theorem"
        )
    )

    private fun loadInitialRoadmap() = listOf(
        RoadmapStage(
            stageNumber = 1,
            title = "Python Core",
            description = "Data structures, OOP, functions, file handling, list comprehensions",
            progressPercent = 100,
            estimatedHours = 25,
            lessonsCompleted = 20,
            totalLessons = 20,
            practiceProblemsDone = 40,
            totalPracticeProblems = 40,
            isCompleted = true
        ),
        RoadmapStage(
            stageNumber = 2,
            title = "NumPy",
            description = "N-dimensional arrays, vectorization, indexing, linear algebra ops",
            progressPercent = 100,
            estimatedHours = 15,
            lessonsCompleted = 12,
            totalLessons = 12,
            practiceProblemsDone = 25,
            totalPracticeProblems = 25,
            isCompleted = true
        ),
        RoadmapStage(
            stageNumber = 3,
            title = "Pandas",
            description = "DataFrames, cleaning missing values, grouping, aggregations, merges",
            progressPercent = 65,
            estimatedHours = 30,
            lessonsCompleted = 15,
            totalLessons = 22,
            practiceProblemsDone = 18,
            totalPracticeProblems = 30,
            isCurrent = true
        ),
        RoadmapStage(
            stageNumber = 4,
            title = "SQL for Analysis",
            description = "Joins, aggregations, window functions, CTEs, subqueries",
            progressPercent = 15,
            estimatedHours = 25,
            lessonsCompleted = 3,
            totalLessons = 18,
            practiceProblemsDone = 6,
            totalPracticeProblems = 35
        ),
        RoadmapStage(
            stageNumber = 5,
            title = "Statistics & Probability",
            description = "Hypothesis testing, distributions, p-values, regression (IITM aligned)",
            progressPercent = 45,
            estimatedHours = 35,
            lessonsCompleted = 12,
            totalLessons = 25,
            practiceProblemsDone = 20,
            totalPracticeProblems = 45
        ),
        RoadmapStage(
            stageNumber = 6,
            title = "Data Visualization",
            description = "Matplotlib, Seaborn, interactive charts, dashboard insights",
            progressPercent = 25,
            estimatedHours = 20,
            lessonsCompleted = 4,
            totalLessons = 16,
            practiceProblemsDone = 5,
            totalPracticeProblems = 20
        ),
        RoadmapStage(
            stageNumber = 7,
            title = "Machine Learning Basics",
            description = "Scikit-Learn, linear/logistic regression, decision trees, evaluation metrics",
            progressPercent = 0,
            estimatedHours = 40,
            lessonsCompleted = 0,
            totalLessons = 25,
            practiceProblemsDone = 0,
            totalPracticeProblems = 30
        ),
        RoadmapStage(
            stageNumber = 8,
            title = "Portfolio Projects",
            description = "Real-world dataset end-to-end analysis published on GitHub",
            progressPercent = 35,
            estimatedHours = 30,
            lessonsCompleted = 1,
            totalLessons = 3,
            practiceProblemsDone = 1,
            totalPracticeProblems = 3
        ),
        RoadmapStage(
            stageNumber = 9,
            title = "Internship Applications",
            description = "Resume polishing, cold outreach, applying for ₹5k-6k/month roles",
            progressPercent = 20,
            estimatedHours = 20,
            lessonsCompleted = 2,
            totalLessons = 10,
            practiceProblemsDone = 8,
            totalPracticeProblems = 25
        )
    )

    private fun loadInitialProjects() = listOf(
        PortfolioProject(
            id = "proj_1",
            title = "Student Performance Analyzer",
            description = "End-to-end Python exploratory data analysis examining study hours, attendance, and CGPA correlation with actionable insights.",
            goal = "Demonstrate practical data analysis & visualization skills for internship applications.",
            techStack = listOf("Python", "Pandas", "NumPy", "Matplotlib", "Seaborn"),
            githubUrl = "https://github.com/ravi/student-performance-analyzer",
            status = "In Progress",
            progressPercent = 46,
            tasks = listOf(
                ProjectTask("pt_1", "Find dataset", true),
                ProjectTask("pt_2", "Load dataset into Pandas", true),
                ProjectTask("pt_3", "Inspect dataset info & summary stats", true),
                ProjectTask("pt_4", "Clean data & handle missing values", true),
                ProjectTask("pt_5", "Analyze score averages by group", true),
                ProjectTask("pt_6", "Analyze study hours vs performance", true),
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
            appliedDate = "2026-09-28",
            followUpDate = "2026-10-06",
            status = InternshipStatus.INTERVIEW,
            notes = "Interview round 1 completed. Python test scheduled for Thursday."
        ),
        InternshipApplication(
            id = "intern_2",
            company = "GrowthMetrics Lab",
            role = "Data Science Trainee",
            stipend = "₹5,000/month",
            location = "Remote",
            appliedDate = "2026-10-01",
            followUpDate = "2026-10-08",
            status = InternshipStatus.SHORTLISTED,
            notes = "Resume shortlisted on Internshala."
        ),
        InternshipApplication(
            id = "intern_3",
            company = "FinTech Analytics",
            role = "Python Automation Intern",
            stipend = "₹6,000/month",
            location = "Hybrid",
            appliedDate = "2026-10-03",
            followUpDate = "2026-10-10",
            status = InternshipStatus.APPLIED,
            notes = "Applied via LinkedIn with portfolio link."
        ),
        InternshipApplication(
            id = "intern_4",
            company = "Research Foundation",
            role = "Data Assistant",
            stipend = "₹5,500/month",
            location = "Remote",
            appliedDate = "2026-09-20",
            followUpDate = "2026-09-30",
            status = InternshipStatus.REJECTED,
            notes = "Required final year student. Valuable feedback received."
        )
    )

    private fun loadInitialExams() = listOf(
        ExamTrack(
            name = "GATE",
            paper = "Mathematics (MA)",
            status = "Future / Exploration",
            syllabusProgress = 22,
            pyqCompleted = 45,
            totalPyqs = 500,
            targetYear = "2028",
            targetInstitutes = "IITs / IISc",
            isActive = false
        ),
        ExamTrack(
            name = "JAM",
            paper = "Mathematics (MA)",
            status = "Exploration",
            syllabusProgress = 35,
            pyqCompleted = 60,
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

    private fun loadInitialHealthLog() = HealthLog(
        date = "Today",
        gymCompleted = true,
        workoutType = WorkoutType.PULL,
        workoutDurationMinutes = 45,
        steps = 6420,
        sleepHours = 7.2,
        waterGlasses = 8,
        moodRating = 4,
        energyRating = 4
    )

    private fun loadInitialHabits() = listOf(
        HabitItem("h_1", "Study (Academics)", "School", 7, true, listOf(true, true, true, true, true, true, true)),
        HabitItem("h_2", "Coding / Data Science", "Code", 5, true, listOf(true, false, true, true, true, true, true)),
        HabitItem("h_3", "Gym / Physical Workout", "FitnessCenter", 4, true, listOf(false, true, true, false, true, true, true)),
        HabitItem("h_4", "7+ Hours Sleep", "Bedtime", 6, true, listOf(true, true, true, true, false, true, true)),
        HabitItem("h_5", "Reading Math Concepts", "MenuBook", 3, false, listOf(false, true, true, true, false, true, false)),
        HabitItem("h_6", "No Mindless Scrolling", "TimerOff", 5, true, listOf(true, true, false, true, true, true, true)),
        HabitItem("h_7", "Daily Evening Review", "RateReview", 7, false, listOf(true, true, true, true, true, true, false))
    )

    private fun loadInitialDistractionLog() = DistractionLog(
        date = "Today",
        instagramMinutes = 25,
        youtubeMinutes = 65,
        gamingMinutes = 0,
        otherMinutes = 40,
        yesterdayTotalMinutes = 165
    )

    private fun loadInitialJournal() = DailyJournal(
        id = "j_today",
        date = "Today",
        whatWentWell = "Finished 50 min deep work on Bayes theorem and hit the gym on time.",
        whatWentWrong = "Lost 30 mins browsing YouTube before study block.",
        whatToImproveTomorrow = "Keep phone in another room during 2 PM study session.",
        moodRating = 4,
        energyRating = 4,
        aiPatternInsight = "Your highest focus hours occur between 2 PM and 5 PM when distraction time is under 30 mins."
    )

    private fun loadInitialChatMessages() = listOf(
        ChatMessage(
            id = "m_0",
            sender = MessageSender.AI,
            text = "Good evening Ravi! Main aapka Focus OS AI Mentor hoon.\n\nAapka Supabase backend connected hai.\n\nAapki current do primary priorities hain:\n1. 🎯 **CGPA Recovery** (ITEP 6.8 & IITM 5.5)\n2. 📊 **Data Science & Paid Internship** (₹5k-6k/mo target)\n\nBatao bhai, aaj kis cheez par kaam karna hai?",
            quickActionSuggestions = listOf("Bhai aaj kya karu?", "Kal mera IITM assignment hai", "Meri CGPA kaise improve hogi?", "Next 2 hours plan")
        )
    )
}
