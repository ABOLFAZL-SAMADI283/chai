-- ═══════════════════════════════════════════════════════════════
--  نسخهٔ ۱.۱ — همگام‌سازی درست، دوئل، چالش روزانه و لیگ خودکار
-- ═══════════════════════════════════════════════════════════════

-- ۱) کلید یکتای سمت کلاینت تا همگام‌سازی تکراری نسخه ندهد
alter table public.sessions add column if not exists client_id text;
create unique index if not exists sessions_client_idx
  on public.sessions (user_id, client_id) where client_id is not null;

-- ۲) user_id را سرور می‌گذارد (کلاینت اجازهٔ جعل ندارد)
create or replace function public.trg_sessions_set_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  if new.user_id is null then
    new.user_id := auth.uid();
  end if;
  return new;
end $$;

drop trigger if exists sessions_set_user on public.sessions;
create trigger sessions_set_user
  before insert on public.sessions
  for each row execute function public.trg_sessions_set_user();

-- ۳) لیگ و امتیاز هفتگی، خودکار با هر جلسهٔ جدید
create or replace function public.trg_refresh_league()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  update public.profiles p
     set week_score = coalesce((select sum(actual_sec)/60 from public.sessions s
                                 where s.user_id = p.id
                                   and s.started_at >= date_trunc('week', now())), 0),
         league = case
           when coalesce((select sum(actual_sec)/60 from public.sessions s
                          where s.user_id = p.id
                            and s.started_at >= date_trunc('week', now())), 0) >= 1200 then 'platinum'
           when coalesce((select sum(actual_sec)/60 from public.sessions s
                          where s.user_id = p.id
                            and s.started_at >= date_trunc('week', now())), 0) >= 600 then 'gold'
           when coalesce((select sum(actual_sec)/60 from public.sessions s
                          where s.user_id = p.id
                            and s.started_at >= date_trunc('week', now())), 0) >= 240 then 'silver'
           else 'bronze' end
   where p.id = new.user_id;
  return new;
end $$;

drop trigger if exists sessions_refresh_league on public.sessions;
create trigger sessions_refresh_league
  after insert on public.sessions
  for each row execute function public.trg_refresh_league();

-- ۴) دوئل‌های من
create or replace function public.my_duels()
returns table (id uuid, host_name text, guest_name text, duration_min integer,
               host_sec integer, guest_sec integer, status text, created_at timestamptz)
language sql stable security definer set search_path = public as $$
  select d.id,
         coalesce(h.nickname, '—'),
         coalesce(g.nickname, '—'),
         d.duration_min, d.host_sec, d.guest_sec, d.status, d.created_at
    from public.duels d
    left join public.profiles h on h.id = d.host_id
    left join public.profiles g on g.id = d.guest_id
   where d.host_id = auth.uid() or d.guest_id = auth.uid()
   order by d.created_at desc
   limit 20;
$$;

-- ۵) ساخت دوئل با نام کاربر یا نام نمایشی
create or replace function public.create_duel(p_guest text, p_duration_min integer default 30)
returns uuid language plpgsql security definer set search_path = public as $$
declare
  v_host uuid := auth.uid();
  v_guest uuid;
  v_id uuid;
begin
  if v_host is null then raise exception 'وارد نشده‌اید'; end if;
  select p.id into v_guest
    from public.profiles p
   where lower(p.nickname) = lower(p_guest) or lower(p.username) = lower(p_guest)
   limit 1;
  if v_guest is null then raise exception 'کاربری با این نام پیدا نشد'; end if;
  if v_guest = v_host then raise exception 'نمی‌توانی با خودت دوئل کنی'; end if;
  insert into public.duels (host_id, guest_id, duration_min)
  values (v_host, v_guest, coalesce(p_duration_min, 30))
  returning id into v_id;
  return v_id;
end $$;

-- ۶) چالش روزانه
create or replace function public.daily_challenge()
returns table (day date, goal text, duration_min integer, total_sec bigint, participants integer)
language sql stable as $$
  select coalesce(d.day, current_date),
         coalesce(d.goal, 'study'),
         coalesce(d.duration_min, 30),
         coalesce(d.total_sec, 0),
         coalesce(d.participants, 0)
    from (select 1) x
    left join public.daily_challenges d on d.day = current_date;
$$;

create or replace function public.join_daily_challenge(p_goal text default null, p_minutes integer default 30)
returns integer language plpgsql security definer set search_path = public as $$
declare n integer;
begin
  insert into public.daily_challenges (day, goal, duration_min, participants)
  values (current_date, coalesce(p_goal, 'study'), coalesce(p_minutes, 30), 1)
  on conflict (day) do update set participants = daily_challenges.participants + 1;
  select participants into n from public.daily_challenges where day = current_date;
  return n;
end $$;
