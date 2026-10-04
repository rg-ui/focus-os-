# FOCUS OS 📱
> *"Focus on what moves your life forward."*

A production-grade Android application crafted with **Kotlin + Jetpack Compose**, tailored as a private personal operating system for managing dual degrees, CGPA recovery, Data Science roadmap, ₹5k-6k/mo internship pipeline, health & fitness, focus sessions, and context-aware AI mentorship without overwhelm.

---

## 🚀 APK Location
The compiled, ready-to-install Android APK is generated at:
- **`FocusOS-v1.0.apk`** (17 MB)
- `app/build/outputs/apk/debug/app-debug.apk`

To install on your connected Android device or emulator:
```bash
adb install -r "FocusOS-v1.0.apk"
```

---

## 🌟 Key Features

### 1. 🏠 Home Dashboard
- Dynamic greeting based on time of day ("Good evening, Ravi.")
- **Today's Score Ring (72/100)**: Encouraging circular activity score calculated from academic progress, focus sessions, health, and priority completion.
- **Top 3 Priorities**: Auto-recommended by AI based on upcoming assignments and deadlines.
- **Dual Degree & DS Roadmap Snapshot**: Live view of ITEP (6.8), IITM (5.5), Data Science progress (32%), and internship applications (8).
- **AI Insight Callout**: Proactive guidance preventing overplanning.

### 2. 📅 Today's Timeline
- **Classes**: ITEP and IITM classes with interactive status (`Present`, `Absent`, `Watched recording`, `Need revision`).
- **Missed Class Auto-Recovery**: Marking a class absent automatically creates a 4-step recovery task (*Watch lecture*, *Complete notes*, *Attempt quiz*, *Revise*).
- **Categorized Tasks**: High/Medium/Low priority tags, category badges, and quick "+" task creator.
- **Focus Timer**: 25m, 50m, 90m Pomodoro/Deep Work timer with countdown, background notifications, and accomplishment reflection logger.

### 3. 🎓 Academics (Dual Degree & CGPA Simulator)
- **ITEP B.Sc. B.Ed. Mathematics** (Year 2, Current CGPA: 6.8, Target: 7.5)
- **IIT Madras BS Data Science** (Year 1, Current CGPA: 5.5, Target: 6.5)
- **Interactive SGPA Calculator**: Select target CGPA buttons (7.0, 7.5, 8.0, 8.5) to dynamically project the exact required SGPA across remaining credits.
- **Subject Tracker**: Real Analysis, Abstract Algebra, Statistics 1, Computational Thinking, etc., with health indicators, difficulty, and next exam/assignment dates.

### 4. 💼 Career, Projects & Internship Tracker
- **9-Stage Data Science Learning Roadmap**: Python → NumPy → Pandas → SQL → Statistics → Data Viz → ML → Projects → Internship.
- **Mini Project**: *Student Performance Analyzer* with 13 interactive milestone tasks.
- **Internship Pipeline**: Track applications for ₹5,000–₹6,000/month roles with real-time conversion metrics (Application → Response rate, Interview rate).
- **GATE / JAM / SSC Exploration**: Kept dormant/exploratory to protect academic focus, with interactive AI comparison advisor.

### 5. 🏋️‍♂️ Health & Mindful Habits
- **Apple Health-Style UI**: Gym workout tracker (Push/Pull/Legs/Cardio, duration), steps (6,420), sleep (7.2h), water (8 glasses), mood & energy.
- **7 Core Habits**: 7-day consistency dots and active streak counters.
- **Screen Time & Distraction Tracker**: Instagram, YouTube, Gaming, Other with daily comparison (*"You reduced distraction by 35 minutes today!"*).
- **Daily Evening Journal**: What went well, what went wrong, what to improve tomorrow.

### 6. 🧠 AI Mentor & Assistant
- **Context-Aware In-App Chat**: Aware of your CGPAs, today's tasks, deadlines, and priorities.
- **Ready Prompts**: *"Bhai aaj kya karu?"*, *"Kal mera IITM assignment hai..."*, *"Meri CGPA kaise improve hogi?"*, *"GATE karu ya internship?"*.
- **Daily Evening Check-In Modal**: 4 questions generating your daily review and tomorrow's top 3 priorities.
- **Morning AI Brief**: Daily schedule, priority notices, and recommended study blocks.

---

## 🗄️ Supabase PostgreSQL Backend

A complete `supabase_schema.sql` file is included in the project root containing:
- 24+ relational tables (`profiles`, `degrees`, `subjects`, `classes`, `tasks`, `focus_sessions`, `habits`, `health_logs`, `projects`, `internships`, `daily_reviews`, `ai_messages`, etc.)
- Strict **Row Level Security (RLS)** policies ensuring 100% private data access.

---

## 🛠️ Architecture & Tech Stack
- **UI Toolkit**: Jetpack Compose + Material 3 (Apple-inspired design language, custom SF typography hierarchy, deep blue `#0A84FF` accent, subtle glassmorphism borders)
- **Language**: Kotlin 2.0.21
- **Architecture**: MVVM + Repository Pattern + Reactive StateFlows
- **Network & Caching**: OkHttp + SharedPreferences/JSON persistent fallback with full offline support
- **Notifications**: Android NotificationManager with high-priority channels for classes, deadlines, and daily check-ins.
