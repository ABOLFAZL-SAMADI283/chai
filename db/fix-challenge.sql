-- فیکس چالش روزانه: هر کاربر فقط یک‌بار در روز شمرده می‌شود
create table if not exists public.daily_challenge_joins (
  day date not null,
  user_id uuid not null references public.profiles(id) on delete cascade,
  goal text,
  duration_min integer,
  joined_at timestamptz not null default now(),
  primary key (day, user_id)
);

-- تابع نمایش چالش: تعداد = نفرات یکتا
create or replace function public.daily_challenge()
returns table (day date, goal text, duration_min integer, total_sec bigint, participants integer)
language sql stable as $$
  select coalesce(d.day, current_date),
         coalesce(d.goal, 'study'),
         coalesce(d.duration_min, 30),
         coalesce(d.total_sec, 0),
         (select count(*)::int from public.daily_challenge_joins j where j.day = current_date)
    from (select 1) x
    left join public.daily_challenges d on d.day = current_date;
$$;

-- تابع ثبت‌نام چالش: idempotent (تکرار بی‌اثر)
create or replace function public.join_daily_challenge(p_goal text default null, p_minutes integer default 30)
returns integer language plpgsql security definer set search_path = public as $$
declare n integer;
begin
  insert into public.daily_challenges (day, goal, duration_min, participants)
  values (current_date, coalesce(p_goal, 'study'), coalesce(p_minutes, 30), 0)
  on conflict (day) do nothing;

  insert into public.daily_challenge_joins (day, user_id, goal, duration_min)
  values (current_date, auth.uid(), coalesce(p_goal, 'study'), coalesce(p_minutes, 30))
  on conflict (day, user_id) do nothing;

  select count(*)::int into n from public.daily_challenge_joins where day = current_date;
  update public.daily_challenges set participants = n, goal = coalesce(goal, coalesce(p_goal, 'study')) where day = current_date;
  return n;
end $$;

-- پاک‌کردن داده‌های تست قدیمی
delete from public.daily_challenges where day < current_date;
update public.daily_challenges set participants = 0;
