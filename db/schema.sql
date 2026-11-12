-- ═══════════════════════════════════════════════════════════════
--  چای | دیتابیس (Supabase / Postgres)
--  بدون محتوای کاربر → فقط عدد و نام مستعار (مسئولیت حقوقی ≈ صفر)
-- ═══════════════════════════════════════════════════════════════

-- ── ۱. پروفایل ────────────────────────────────────────────────
create table if not exists public.profiles (
  id          uuid primary key default gen_random_uuid(),
  username    text unique,                    -- نام کاربری (lowercase)، nullable تا وقتی ثبت‌نام کرده
  nickname    text not null check (char_length(nickname) between 2 and 12),
  avatar      text not null default '☕',
  created_at  timestamptz not null default now(),
  total_sec   integer not null default 0,     -- مجموع تمرکز (کش محاسباتی)
  streak      integer not null default 0,
  week_score  integer not null default 0,     -- امتیاز لیگ هفتگی
  league      text not null default 'bronze'   -- bronze | silver | gold
);

create index if not exists profiles_week_score_idx on public.profiles (week_score desc);

-- ── ۲. جلسات تمرکز ────────────────────────────────────────────
create table if not exists public.sessions (
  id          uuid primary key default gen_random_uuid(),
  user_id     uuid not null references public.profiles(id) on delete cascade,
  goal        text not null,                  -- study | book | work | gym | sleep
  planned_sec integer not null,
  actual_sec  integer not null default 0,
  completed   boolean not null default false,
  started_at  timestamptz not null default now(),
  synced      boolean not null default true   -- برای همگام‌سازی آفلاین
);
create index if not exists sessions_user_idx on public.sessions (user_id, started_at desc);

-- ── ۳. اعتبارنامه (نام کاربری + هش کد بازیابی) ───────────────
-- کد بازیابی هرگز به‌صورت خام ذخیره نمی‌شود؛ فقط هش SHA-256.
-- email: ایمیل ساختگی که برای ورود به Supabase Auth استفاده می‌شود
--        (کاربر هیچ ایمیلی نمی‌بیند و ایمیلی هم دریافت نمی‌کند)
create table if not exists public.credentials (
  username      text primary key,             -- همیشه lowercase
  user_id       uuid not null unique references public.profiles(id) on delete cascade,
  email         text not null,                -- ایمیل ساختگی برای Auth
  recovery_hash text not null,                -- sha256(کد بازیابی)
  created_at    timestamptz not null default now()
);

-- ── ۴. دوئل (رقابت عددی، بدون چت) ────────────────────────────
create table if not exists public.duels (
  id           uuid primary key default gen_random_uuid(),
  host_id      uuid not null references public.profiles(id) on delete cascade,
  guest_id     uuid references public.profiles(id) on delete cascade,
  duration_min integer not null default 30,
  host_sec     integer not null default 0,
  guest_sec    integer not null default 0,
  status       text not null default 'open',   -- open | finished
  created_at   timestamptz not null default now(),
  expires_at   timestamptz not null default (now() + interval '24 hours')
);
create index if not exists duels_host_idx on public.duels (host_id, created_at desc);

-- ── ۵. چالش روزانه ───────────────────────────────────────────
create table if not exists public.daily_challenges (
  day        date primary key default current_date,
  goal       text not null,
  duration_min integer not null default 30,
  total_sec  bigint not null default 0,
  participants integer not null default 0
);

-- ── ۶. بازدید و کاربران فعال (شمارندهٔ انگیزه) ───────────────
-- هر باز شدن اپ یک سطر heartbeat می‌سازد؛ «کاربر فعال» = heartbeat در ۶ دقیقهٔ اخیر
create table if not exists public.active_sessions (
  id         uuid primary key default gen_random_uuid(),
  anon_id    text,
  goal       text,
  user_id    uuid references public.profiles(id) on delete set null,
  created_at timestamptz not null default now(),
  last_seen  timestamptz not null default now()
);
create index if not exists active_sessions_seen_idx on public.active_sessions (last_seen desc);

-- بازدید روزانه (تعداد کل باز شدن اپ در هر روز)
create table if not exists public.app_visits (
  day        date primary key default current_date,
  opens      integer not null default 0,
  updated_at timestamptz not null default now()
);

alter table public.active_sessions enable row level security;
alter table public.app_visits    enable row level security;

-- فقط از طریق RPC (service role) — دسترسی مستقیم کاربر بسته است
drop policy if exists "active read" on public.active_sessions;
create policy "active read" on public.active_sessions for select using (true);

-- ثبت بازدید + heartbeat (بدون نیاز به ورود به حساب)
create or replace function public.log_visit(p_anon text, p_goal text)
returns void
language sql security definer as $$
  insert into public.active_sessions (anon_id, goal) values (p_anon, p_goal);
  insert into public.app_visits (day, opens) values (current_date, 1)
    on conflict (day) do update set opens = public.app_visits.opens + 1, updated_at = now();
$$;

-- چند نفر همین الان فعال هستند (+ تفکیک بر اساس هدف)
create or replace function public.active_count()
returns table (total bigint, by_goal jsonb, today_opens integer)
language sql stable security definer as $$
  select
    (select count(*) from public.active_sessions where last_seen > now() - interval '6 minutes')::bigint as total,
    coalesce((
      select jsonb_object_agg(goal, cnt)
      from (select goal, count(*) cnt from public.active_sessions
            where last_seen > now() - interval '6 minutes' group by goal) t
    ), '{}'::jsonb) as by_goal,
    (select opens from public.app_visits where day = current_date) as today_opens;
$$;

-- پاکسازی heartbeatهای قدیمی (هفته‌ای یک‌بار کافی است)
create or replace function public.cleanup_active_sessions()
returns void language sql security definer as $$
  delete from public.active_sessions where last_seen < now() - interval '2 days';
$$;

-- ── ۷. مسیر توابع Edge (نام → slug) ─────────────────────────
-- اگر slug توابع UUID باشد (حالت API)، اپ از این جدول مسیر درست را می‌خواند.
create table if not exists public.function_routes (
  name       text primary key,
  slug       text not null,
  updated_at timestamptz not null default now()
);

alter table public.function_routes enable row level security;
drop policy if exists "routes read" on public.function_routes;
create policy "routes read" on public.function_routes for select using (true);

-- ═══════════════════════════════════════════════════════════════
--  امنیت: RLS
--  هر کس فقط سشن‌های خودش را می‌بیند/می‌نویسد.
--  جدول رتبه‌بندی فقط خواندنی است.
-- ═══════════════════════════════════════════════════════════════
alter table public.profiles        enable row level security;
alter table public.sessions       enable row level security;
alter table public.duels          enable row level security;
alter table public.credentials    enable row level security;
alter table public.daily_challenges enable row level security;

-- پروفایل: هر کس فقط سطر خودش را می‌بیند و ویرایش می‌کند
drop policy if exists "own profile" on public.profiles;
create policy "own profile" on public.profiles
  for all using (id = auth.uid()) with check (id = auth.uid());

-- سشن‌ها: فقط مال خودم
drop policy if exists "own sessions" on public.sessions;
create policy "own sessions" on public.sessions
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

-- جدول رتبه‌بندی: همه می‌توانند بخوانند، کسی نمی‌نویسد
drop policy if exists "public leaderboard" on public.profiles;
create policy "public leaderboard" on public.profiles
  for select using (true);

-- دوئل: طرفین فقط
drop policy if exists "duel participants" on public.duels;
create policy "duel participants" on public.duels
  for select using (auth.uid() in (host_id, guest_id));

drop policy if exists "duel update" on public.duels;
create policy "duel update" on public.duels
  for update using (auth.uid() in (host_id, guest_id));

-- چالش روزانه: خواندن عمومی
drop policy if exists "challenge read" on public.daily_challenges;
create policy "challenge read" on public.daily_challenges
  for select using (true);

-- اعتبارنامه: فقط service role (Edge Function) دسترسی دارد — کاربر مستقیم نمی‌بیند
-- (این جدول عمداً بدون policy است)

-- ── ۶. تابع شمارش هفتگی (برای لیگ) ───────────────────────────
create or replace function public.weekly_league()
returns table (id uuid, nickname text, avatar text, total_sec integer, streak integer, week_score integer, league text)
language sql stable as $$
  select p.id, p.nickname, p.avatar,
         coalesce((select sum(s.actual_sec) from public.sessions s
                    where s.user_id = p.id
                      and s.started_at >= date_trunc('week', now())), 0) as total_sec,
         p.streak, p.week_score, p.league
  from public.profiles p
  order by p.week_score desc, total_sec desc
  limit 100;
$$;
