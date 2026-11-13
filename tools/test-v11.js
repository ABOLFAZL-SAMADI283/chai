// تست نسخهٔ ۱.۱: همگام‌سازی سشن + تریگر لیگ + دوئل + چالش روزانه
const fs = require('fs');
const path = require('path');

const sec = JSON.parse(
  fs.readFileSync(path.join(process.env.USERPROFILE, '.config', 'opencode', 'secrets.json'), 'utf8')
    .replace(/^\uFEFF/, '')
);
const URL = sec.SUPABASE_URL;
const PUB = sec.SUPABASE_PUBLISHABLE_KEY;
const SECRET = sec.SUPABASE_SECRET_KEY;
const FN = '/functions/v1';

const H = (key, extra = {}) => ({
  apikey: key, Authorization: `Bearer ${key}`, 'Content-Type': 'application/json', ...extra,
});

// برای درخواست‌های با توکن کاربر: apikey باید کلید عمومی باشد و توکن در Authorization
const UH = (token, extra = {}) => ({
  apikey: PUB, Authorization: `Bearer ${token}`, 'Content-Type': 'application/json', ...extra,
});

async function post(p, body, key = PUB, extra = {}) {
  const isUser = typeof key === 'string' && key.startsWith('ey');
  const r = await fetch(`${URL}${p}`, {
    method: 'POST',
    headers: isUser ? UH(key, extra) : H(key, extra),
    body: JSON.stringify(body),
  });
  const t = await r.text();
  let j = null; try { j = JSON.parse(t); } catch (_) {}
  return { status: r.status, json: j, text: t };
}
async function get(p, key = PUB) {
  const isUser = typeof key === 'string' && key.startsWith('ey');
  const r = await fetch(`${URL}${p}`, { headers: isUser ? UH(key) : H(key) });
  const t = await r.text();
  let j = null; try { j = JSON.parse(t); } catch (_) {}
  return { status: r.status, json: j, text: t };
}

(async () => {
  const stamp = Date.now().toString().slice(-6);
  const A = 'alice' + stamp;
  const B = 'bob' + stamp;

  const rt = await get('/rest/v1/function_routes?select=name,slug', SECRET);
  const R = {};
  (rt.json || []).forEach(x => { R[x.name] = x.slug; });

  console.log('\n۱) ثبت‌نام دو کاربر');
  const ra = await post(`${FN}/${R.register}`, { username: A, nickname: 'آلیس' + stamp.slice(-2) });
  const rb = await post(`${FN}/${R.register}`, { username: B, nickname: 'باب' + stamp.slice(-2) });
  console.log('   آلیس:', ra.status, '| باب:', rb.status);
  if (ra.status !== 200 || rb.status !== 200) {
    console.log('   →', ra.text.slice(0, 300), rb.text.slice(0, 300));
    process.exit(1);
  }
  const tokA = ra.json.access_token;
  const tokB = rb.json.access_token;
  const idA = ra.json.user_id;
  const idB = rb.json.user_id;
  const nickA = ra.json.nickname;
  console.log('   id آلیس:', idA);

  console.log('\n۲) درج سشن با client_id (بدون user_id — سرور می‌گذارد)');
  const sess = {
    client_id: 's_test_' + stamp,
    goal: 'study',
    planned_sec: 1800,
    actual_sec: 1500,
    completed: true,
    started_at: new Date().toISOString(),
  };
  const ins = await post('/rest/v1/sessions', [sess], tokA, { Prefer: 'resolution=ignore-duplicates' });
  console.log('   insert →', ins.status, ins.text.slice(0, 200));

  console.log('\n۳) درج تکراری (نباید ردیف دوم بسازد)');
  const ins2 = await post('/rest/v1/sessions', [sess], tokA, { Prefer: 'resolution=ignore-duplicates' });
  console.log('   insert2 →', ins2.status, ins2.text.slice(0, 160));
  const mine = await get('/rest/v1/sessions?select=id,goal,actual_sec,client_id', tokA);
  console.log('   سشن‌های آلیس:', JSON.stringify(mine.json));

  console.log('\n۴) تریگر لیگ — week_score و league باید به‌روز شده باشند');
  const prof = await get('/rest/v1/profiles?select=nickname,week_score,league', SECRET);
  const pa = (prof.json || []).find(p => p.nickname === nickA);
  console.log('   پروفایل آلیس:', JSON.stringify(pa));
  console.log('   (۱۵۰۰ ثانیه = ۲۵ دقیقه → انتظار: week_score=25، league=bronze)');

  console.log('\n۵) چالش روزانه');
  const dc = await post('/rest/v1/rpc/daily_challenge', {}, PUB);
  console.log('   daily_challenge →', dc.status, JSON.stringify(dc.json));
  const jd = await post('/rest/v1/rpc/join_daily_challenge', { p_goal: 'study', p_minutes: 30 }, tokA);
  console.log('   join →', jd.status, JSON.stringify(jd.json));

  console.log('\n۶) دوئل — باب آلیس را به دوئل دعوت می‌کند');
  const cd = await post('/rest/v1/rpc/create_duel', { p_guest: nickA, p_duration_min: 30 }, tokB);
  console.log('   create_duel →', cd.status, JSON.stringify(cd.json));
  const md = await post('/rest/v1/rpc/my_duels', {}, tokA);
  console.log('   my_duels (آلیس) →', md.status, JSON.stringify(md.json).slice(0, 300));
  const md2 = await post('/rest/v1/rpc/my_duels', {}, tokB);
  console.log('   my_duels (باب) →', md2.status, JSON.stringify(md2.json).slice(0, 300));

  console.log('\n۷) لیگ هفتگی');
  const wl = await post('/rest/v1/rpc/weekly_league', {}, tokA);
  console.log('   →', JSON.stringify(wl.json).slice(0, 400));

  console.log('\n۸) کاربر دیگری نمی‌تواند سشن آلیس را ببیند (RLS)');
  const other = await get('/rest/v1/sessions?select=id', tokB);
  console.log('   سشن‌های باب:', JSON.stringify(other.json));

  console.log('\n✔ تمام تست‌ها اجرا شد\n');
})();
