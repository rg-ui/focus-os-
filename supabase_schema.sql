-- ============================================================================
-- FOCUS OS - Complete Supabase PostgreSQL Schema with Row Level Security (RLS)
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    avatar_url TEXT,
    major_goal_1 TEXT DEFAULT 'CGPA Recovery',
    major_goal_2 TEXT DEFAULT 'Data Science & Internship',
    current_streak_days INT DEFAULT 0,
    today_score INT DEFAULT 70,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. DEGREES
CREATE TABLE IF NOT EXISTS public.degrees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    type TEXT NOT NULL, -- 'ITEP' or 'IITM'
    name TEXT NOT NULL,
    current_year INT NOT NULL,
    total_years INT NOT NULL,
    current_cgpa NUMERIC(3,2) NOT NULL,
    target_cgpa NUMERIC(3,2) NOT NULL,
    completed_credits INT NOT NULL,
    total_credits INT NOT NULL,
    current_semester INT NOT NULL,
    is_primary_focus BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. SEMESTERS
CREATE TABLE IF NOT EXISTS public.semesters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    degree_id UUID NOT NULL REFERENCES public.degrees(id) ON DELETE CASCADE,
    semester_number INT NOT NULL,
    sgpa NUMERIC(3,2),
    credits INT NOT NULL,
    is_completed BOOLEAN DEFAULT false
);

-- 4. SUBJECTS
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    degree_id UUID NOT NULL REFERENCES public.degrees(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    credits INT NOT NULL,
    current_score NUMERIC(5,2) DEFAULT 0,
    target_score NUMERIC(5,2) DEFAULT 85,
    difficulty TEXT DEFAULT 'Medium',
    progress_percent INT DEFAULT 0,
    status TEXT DEFAULT 'LEARNING',
    next_exam_date DATE,
    next_assignment_date TIMESTAMPTZ,
    attendance_percent INT DEFAULT 100,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. CLASSES & SCHEDULE
CREATE TABLE IF NOT EXISTS public.classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES public.subjects(id) ON DELETE SET NULL,
    subject_name TEXT NOT NULL,
    degree_type TEXT NOT NULL,
    time_slot TEXT NOT NULL,
    topic TEXT NOT NULL,
    status TEXT DEFAULT 'UPCOMING', -- 'UPCOMING', 'PRESENT', 'ABSENT', 'WATCHED_RECORDING', 'NEED_REVISION'
    date DATE DEFAULT CURRENT_DATE
);

-- 6. MISSED CLASSES & RECOVERY
CREATE TABLE IF NOT EXISTS public.missed_classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    class_id UUID REFERENCES public.classes(id) ON DELETE CASCADE,
    subject_name TEXT NOT NULL,
    degree_type TEXT NOT NULL,
    topic TEXT NOT NULL,
    missed_date DATE NOT NULL,
    watch_lecture_done BOOLEAN DEFAULT false,
    notes_done BOOLEAN DEFAULT false,
    quiz_done BOOLEAN DEFAULT false,
    revision_done BOOLEAN DEFAULT false,
    is_recovered BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. TASKS
CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    category TEXT NOT NULL, -- 'ITEP', 'IITM', 'DATA_SCIENCE', 'INTERNSHIP', 'PROJECT', etc.
    priority TEXT DEFAULT 'MEDIUM',
    estimated_minutes INT DEFAULT 45,
    deadline TIMESTAMPTZ,
    is_completed BOOLEAN DEFAULT false,
    is_top_3 BOOLEAN DEFAULT false,
    is_catch_up BOOLEAN DEFAULT false,
    related_subject TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. FOCUS SESSIONS
CREATE TABLE IF NOT EXISTS public.focus_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    duration_minutes INT NOT NULL,
    task_title TEXT NOT NULL,
    category TEXT NOT NULL,
    accomplishment_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. HABITS & LOGS
CREATE TABLE IF NOT EXISTS public.habits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    icon_name TEXT NOT NULL,
    current_streak INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.habit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    habit_id UUID NOT NULL REFERENCES public.habits(id) ON DELETE CASCADE,
    date DATE DEFAULT CURRENT_DATE,
    completed BOOLEAN DEFAULT true,
    UNIQUE(habit_id, date)
);

-- 10. HEALTH & DISTRACTION LOGS
CREATE TABLE IF NOT EXISTS public.health_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    date DATE DEFAULT CURRENT_DATE,
    gym_completed BOOLEAN DEFAULT false,
    workout_type TEXT DEFAULT 'PUSH',
    workout_duration_minutes INT DEFAULT 0,
    steps INT DEFAULT 0,
    sleep_hours NUMERIC(3,1) DEFAULT 7.0,
    water_glasses INT DEFAULT 8,
    mood_rating INT DEFAULT 4,
    energy_rating INT DEFAULT 4,
    instagram_minutes INT DEFAULT 0,
    youtube_minutes INT DEFAULT 0,
    gaming_minutes INT DEFAULT 0,
    other_distraction_minutes INT DEFAULT 0,
    UNIQUE(user_id, date)
);

-- 11. PROJECTS & PROJECT TASKS
CREATE TABLE IF NOT EXISTS public.projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    goal TEXT,
    tech_stack TEXT[],
    github_url TEXT,
    status TEXT DEFAULT 'In Progress',
    progress_percent INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.project_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES public.projects(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    is_done BOOLEAN DEFAULT false,
    position INT DEFAULT 0
);

-- 12. INTERNSHIPS & APPLICATIONS
CREATE TABLE IF NOT EXISTS public.internship_applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    company TEXT NOT NULL,
    role TEXT NOT NULL,
    stipend TEXT DEFAULT '₹6,000/month',
    location TEXT DEFAULT 'Remote',
    status TEXT DEFAULT 'APPLIED',
    link TEXT,
    notes TEXT,
    applied_date DATE DEFAULT CURRENT_DATE,
    follow_up_date DATE
);

-- 13. CAREER & EXAM GOALS
CREATE TABLE IF NOT EXISTS public.exam_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    exam_name TEXT NOT NULL, -- 'GATE', 'JAM', 'SSC'
    paper TEXT NOT NULL,
    status TEXT DEFAULT 'Exploration',
    syllabus_progress INT DEFAULT 0,
    pyq_completed INT DEFAULT 0,
    total_pyqs INT DEFAULT 500,
    is_active BOOLEAN DEFAULT false
);

-- 14. JOURNAL & DAILY REVIEWS
CREATE TABLE IF NOT EXISTS public.journal_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    date DATE DEFAULT CURRENT_DATE,
    what_went_well TEXT,
    what_went_wrong TEXT,
    what_to_improve_tomorrow TEXT,
    mood_rating INT DEFAULT 4,
    energy_rating INT DEFAULT 4,
    ai_pattern_insight TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.daily_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    date DATE DEFAULT CURRENT_DATE,
    completed_summary TEXT,
    missed_summary TEXT,
    focused_hours NUMERIC(4,2) DEFAULT 0,
    tomorrow_priorities TEXT[],
    ai_verdict TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 15. AI CONVERSATIONS & MESSAGES
CREATE TABLE IF NOT EXISTS public.ai_conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT DEFAULT 'Mentorship Session',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.ai_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES public.ai_conversations(id) ON DELETE CASCADE,
    sender TEXT NOT NULL, -- 'USER' or 'AI'
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 16. SETTINGS
CREATE TABLE IF NOT EXISTS public.user_settings (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    theme_mode TEXT DEFAULT 'system',
    morning_brief_time TIME DEFAULT '07:30:00',
    evening_checkin_time TIME DEFAULT '21:30:00',
    max_daily_notifications INT DEFAULT 4,
    notifications_enabled BOOLEAN DEFAULT true,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================================
-- ENABLE ROW LEVEL SECURITY (RLS)
-- ============================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.degrees ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.semesters ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.missed_classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.focus_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.habits ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.habit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.health_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.project_tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.internship_applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exam_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.journal_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_settings ENABLE ROW LEVEL SECURITY;

-- Sample Policy: Users can only read/write their own records
CREATE POLICY "Users manage their own profiles" ON public.profiles FOR ALL USING (auth.uid() = id);
CREATE POLICY "Users manage their own degrees" ON public.degrees FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own tasks" ON public.tasks FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own focus sessions" ON public.focus_sessions FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own habits" ON public.habits FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own health logs" ON public.health_logs FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own projects" ON public.projects FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own internships" ON public.internship_applications FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own journal" ON public.journal_entries FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage their own settings" ON public.user_settings FOR ALL USING (auth.uid() = user_id);
