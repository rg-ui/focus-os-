-- ============================================================================
-- NOVA — Production Multi-User Supabase Schema with Row Level Security (RLS)
-- Tagline: Focus. Progress. Become.
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES & PERSONAL CONTEXT
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    user_type TEXT DEFAULT 'College student',
    avatar_url TEXT,
    onboarding_completed BOOLEAN DEFAULT false,
    priorities TEXT[] DEFAULT ARRAY['Academics', 'Career', 'Focus'],
    goals TEXT[] DEFAULT ARRAY['Improve CGPA', 'Build Career Skills'],
    challenges TEXT[] DEFAULT ARRAY['Phone', 'Procrastination'],
    wake_up_time TEXT DEFAULT '07:00 AM',
    sleep_time TEXT DEFAULT '11:00 PM',
    preferred_study_hours TEXT DEFAULT 'Evenings (4:00 PM - 8:00 PM)',
    gym_preference TEXT DEFAULT 'Regular sessions (4-5 days/wk)',
    reminder_style TEXT DEFAULT 'Balanced',
    major_goal_1 TEXT DEFAULT 'Improve my CGPA',
    major_goal_2 TEXT DEFAULT 'Build strong career skills',
    current_streak_days INT DEFAULT 0,
    today_score INT DEFAULT 75,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. DEGREES & EDUCATION (Multi-Degree support)
CREATE TABLE IF NOT EXISTS public.degrees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    type TEXT NOT NULL, -- 'ITEP' or 'IITM' or 'PRIMARY' / 'SECONDARY'
    name TEXT NOT NULL,
    current_year INT NOT NULL DEFAULT 1,
    total_years INT NOT NULL DEFAULT 4,
    current_cgpa NUMERIC(4,2) NOT NULL DEFAULT 7.0,
    target_cgpa NUMERIC(4,2) NOT NULL DEFAULT 8.5,
    completed_credits INT NOT NULL DEFAULT 0,
    total_credits INT NOT NULL DEFAULT 80,
    current_semester INT NOT NULL DEFAULT 1,
    is_primary_focus BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. SEMESTERS
CREATE TABLE IF NOT EXISTS public.semesters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    degree_id UUID NOT NULL REFERENCES public.degrees(id) ON DELETE CASCADE,
    semester_number INT NOT NULL,
    sgpa NUMERIC(4,2),
    credits INT NOT NULL,
    is_completed BOOLEAN DEFAULT false
);

-- 4. SUBJECTS
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    degree_id UUID NOT NULL REFERENCES public.degrees(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    credits INT NOT NULL DEFAULT 3,
    current_score NUMERIC(5,2) DEFAULT 0,
    target_score NUMERIC(5,2) DEFAULT 85,
    difficulty TEXT DEFAULT 'Medium',
    progress_percent INT DEFAULT 0,
    status TEXT DEFAULT 'LEARNING',
    next_exam_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. SCHEDULED CLASSES
CREATE TABLE IF NOT EXISTS public.classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES public.subjects(id) ON DELETE SET NULL,
    subject_name TEXT NOT NULL,
    day_of_week INT NOT NULL, -- 1 = Monday ... 7 = Sunday
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room_or_link TEXT,
    is_mandatory BOOLEAN DEFAULT true
);

-- 6. MISSED CLASSES & RECOVERY
CREATE TABLE IF NOT EXISTS public.missed_classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    class_id UUID REFERENCES public.classes(id) ON DELETE SET NULL,
    subject_name TEXT NOT NULL,
    missed_date DATE NOT NULL DEFAULT CURRENT_DATE,
    reason TEXT,
    recovery_tasks TEXT[] DEFAULT ARRAY[]::TEXT[],
    is_recovered BOOLEAN DEFAULT false,
    ai_recovery_plan TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. TASKS & TODOS
CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL, -- 'ACADEMIC', 'CAREER', 'HEALTH', 'HABIT', 'PERSONAL'
    priority TEXT NOT NULL DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'URGENT'
    due_date TIMESTAMPTZ,
    is_completed BOOLEAN DEFAULT false,
    estimated_minutes INT DEFAULT 30,
    actual_minutes INT DEFAULT 0,
    energy_level_required TEXT DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH'
    created_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

-- 8. FOCUS SESSIONS
CREATE TABLE IF NOT EXISTS public.focus_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    task_id UUID REFERENCES public.tasks(id) ON DELETE SET NULL,
    duration_minutes INT NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    distraction_count INT DEFAULT 0,
    session_type TEXT DEFAULT 'DEEP_WORK', -- 'POMODORO', 'DEEP_WORK', 'EXAM_PREP'
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. HABITS & TRACKING
CREATE TABLE IF NOT EXISTS public.habits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    target_frequency_days_per_week INT DEFAULT 7,
    current_streak INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    reminder_time TIME,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.habit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    habit_id UUID NOT NULL REFERENCES public.habits(id) ON DELETE CASCADE,
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    is_completed BOOLEAN DEFAULT true,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(habit_id, log_date)
);

-- 10. HEALTH & WELLNESS LOGS
CREATE TABLE IF NOT EXISTS public.health_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    sleep_hours NUMERIC(4,2),
    sleep_quality INT CHECK (sleep_quality BETWEEN 1 AND 5),
    water_intake_liters NUMERIC(3,1) DEFAULT 0,
    gym_completed BOOLEAN DEFAULT false,
    workout_summary TEXT,
    mood_rating INT CHECK (mood_rating BETWEEN 1 AND 5),
    energy_rating INT CHECK (energy_rating BETWEEN 1 AND 5),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, date)
);

-- 11. PROJECTS & PORTFOLIO
CREATE TABLE IF NOT EXISTS public.projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    tech_stack TEXT[],
    github_url TEXT,
    live_demo_url TEXT,
    status TEXT DEFAULT 'IN_PROGRESS', -- 'IDEA', 'IN_PROGRESS', 'COMPLETED', 'DEPLOYED'
    progress_percent INT DEFAULT 0,
    target_completion_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.project_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES public.projects(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    is_completed BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 12. INTERNSHIPS & APPLICATIONS
CREATE TABLE IF NOT EXISTS public.internship_applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    company_name TEXT NOT NULL,
    role_title TEXT NOT NULL,
    application_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status TEXT DEFAULT 'APPLIED', -- 'WISHLIST', 'APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED'
    salary_or_stipend TEXT,
    location TEXT,
    notes TEXT,
    next_round_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 13. EXAM PREPARATION & GOALS
CREATE TABLE IF NOT EXISTS public.exam_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    exam_name TEXT NOT NULL, -- 'GATE CS', 'CAT', 'GRE'
    target_year INT NOT NULL,
    target_score_percentile NUMERIC(5,2),
    syllabus_completed_percent INT DEFAULT 0,
    mock_test_average NUMERIC(5,2) DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 14. JOURNAL & DAILY REVIEWS
CREATE TABLE IF NOT EXISTS public.journal_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    entry_date DATE DEFAULT CURRENT_DATE,
    content TEXT NOT NULL,
    tags TEXT[],
    mood_rating INT DEFAULT 4,
    energy_rating INT DEFAULT 4,
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
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, date)
);

-- 15. USER SETTINGS
CREATE TABLE IF NOT EXISTS public.user_settings (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    theme_mode TEXT DEFAULT 'dark',
    morning_brief_time TIME DEFAULT '07:30:00',
    evening_checkin_time TIME DEFAULT '21:30:00',
    max_daily_notifications INT DEFAULT 4,
    notifications_enabled BOOLEAN DEFAULT true,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) - STRICT USER ISOLATION
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
ALTER TABLE public.user_settings ENABLE ROW LEVEL SECURITY;

-- 1. Profiles RLS
CREATE POLICY "Users can view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can insert own profile" ON public.profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can delete own profile" ON public.profiles FOR DELETE USING (auth.uid() = id);

-- 2. Degrees RLS
CREATE POLICY "Users can view own degrees" ON public.degrees FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own degrees" ON public.degrees FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own degrees" ON public.degrees FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own degrees" ON public.degrees FOR DELETE USING (auth.uid() = user_id);

-- 3. Semesters RLS
CREATE POLICY "Users can view own semesters" ON public.semesters FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own semesters" ON public.semesters FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own semesters" ON public.semesters FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own semesters" ON public.semesters FOR DELETE USING (auth.uid() = user_id);

-- 4. Subjects RLS
CREATE POLICY "Users can view own subjects" ON public.subjects FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own subjects" ON public.subjects FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own subjects" ON public.subjects FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own subjects" ON public.subjects FOR DELETE USING (auth.uid() = user_id);

-- 5. Classes RLS
CREATE POLICY "Users can view own classes" ON public.classes FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own classes" ON public.classes FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own classes" ON public.classes FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own classes" ON public.classes FOR DELETE USING (auth.uid() = user_id);

-- 6. Missed Classes RLS
CREATE POLICY "Users can view own missed classes" ON public.missed_classes FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own missed classes" ON public.missed_classes FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own missed classes" ON public.missed_classes FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own missed classes" ON public.missed_classes FOR DELETE USING (auth.uid() = user_id);

-- 7. Tasks RLS
CREATE POLICY "Users can view own tasks" ON public.tasks FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own tasks" ON public.tasks FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own tasks" ON public.tasks FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own tasks" ON public.tasks FOR DELETE USING (auth.uid() = user_id);

-- 8. Focus Sessions RLS
CREATE POLICY "Users can view own focus sessions" ON public.focus_sessions FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own focus sessions" ON public.focus_sessions FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own focus sessions" ON public.focus_sessions FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own focus sessions" ON public.focus_sessions FOR DELETE USING (auth.uid() = user_id);

-- 9. Habits RLS
CREATE POLICY "Users can view own habits" ON public.habits FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own habits" ON public.habits FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own habits" ON public.habits FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own habits" ON public.habits FOR DELETE USING (auth.uid() = user_id);

-- 10. Habit Logs RLS
CREATE POLICY "Users can view own habit logs" ON public.habit_logs FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own habit logs" ON public.habit_logs FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own habit logs" ON public.habit_logs FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own habit logs" ON public.habit_logs FOR DELETE USING (auth.uid() = user_id);

-- 11. Health Logs RLS
CREATE POLICY "Users can view own health logs" ON public.health_logs FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own health logs" ON public.health_logs FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own health logs" ON public.health_logs FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own health logs" ON public.health_logs FOR DELETE USING (auth.uid() = user_id);

-- 12. Projects RLS
CREATE POLICY "Users can view own projects" ON public.projects FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own projects" ON public.projects FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own projects" ON public.projects FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own projects" ON public.projects FOR DELETE USING (auth.uid() = user_id);

-- 13. Project Tasks RLS
CREATE POLICY "Users can view own project tasks" ON public.project_tasks FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own project tasks" ON public.project_tasks FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own project tasks" ON public.project_tasks FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own project tasks" ON public.project_tasks FOR DELETE USING (auth.uid() = user_id);

-- 14. Internships RLS
CREATE POLICY "Users can view own internships" ON public.internship_applications FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own internships" ON public.internship_applications FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own internships" ON public.internship_applications FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own internships" ON public.internship_applications FOR DELETE USING (auth.uid() = user_id);

-- 15. Journal Entries RLS
CREATE POLICY "Users can view own journal" ON public.journal_entries FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own journal" ON public.journal_entries FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own journal" ON public.journal_entries FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own journal" ON public.journal_entries FOR DELETE USING (auth.uid() = user_id);

-- 16. User Settings RLS
CREATE POLICY "Users can view own settings" ON public.user_settings FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own settings" ON public.user_settings FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own settings" ON public.user_settings FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own settings" ON public.user_settings FOR DELETE USING (auth.uid() = user_id);

-- ============================================================================
-- AUTOMATIC PROFILE CREATION TRIGGER ON SUPABASE AUTH SIGNUP
-- ============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, name)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1))
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
