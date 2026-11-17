// تست کامل بک‌اند برای نسخهٔ ۱.۳ — حساب تست را در پایان پاک می‌کند
const fs = require('fs');
const s = JSON.parse(fs.readFileSync('C:/Users/Abolfazl Samadi/.config/opencode/secrets.json', 'utf8').replace(/^\uFEFF/, ''));
const url = s.SUPABASE_URL, pub = s.SUPABASE_PUBLISHABLE_KEY, key = s.SUPABASE_SECRET_KEY;
const name = 'fin' + Math.floor(Math.random() * 900000);
let pass = 0, fail = 0;
const ok = (label, cond, extra) => {
  console.log((cond ? '✅' : '❌') + ' ' + label + (extra ? ' → ' + extra : ''));
  cond ? pass++ : fail++;
};
(async () => {
  const routes = await fetch(url + '/rest/v1/function_routes?select=name,slug', { headers: { apikey: pub, Authorization: 'Bearer ' + pub } }).then(r => r.json());
  const slug = n => (routes.find(r => r.name === n) || {}).slug || n;

  // ۱) چک نام
  const ck = await fetch(url + '/functions/v1/' + slug('check-username') + '?name=' + name + '&suggest=1', { headers: { apikey: pub } });
  const ckj = await ck.json();
  ok('چک نام آزاد است', ck.status === 200 && ckj.available === true);
  ok('پیشنهاد جایگزین دارد', Array.isArray(ckj.suggestions) && ckj.suggestions.length > 0);

  // ۲) ثبت‌نام
  const reg = await fetch(url + '/functions/v1/' + slug('register'), { method: 'POST', headers: { apikey: pub, 'Content-Type': 'application/json' }, body: JSON.stringify({ username: name, nickname: 'تست‌نهایی' }) });
  const regj = await reg.json();
  ok('ثبت‌نام موفق', reg.status === 200 && !!regj.access_token);
  ok('کد بازیابی داده شد', !!regj.recovery_code && String(regj.recovery_code).length >= 6);
  const token = regj.access_token;
  const H = { apikey: pub, Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' };

  // ۳) ثبت‌نام تکراری → ارور «تمیز»
  const reg2 = await fetch(url + '/functions/v1/' + slug('register'), { method: 'POST', headers: { apikey: pub, 'Content-Type': 'application/json' }, body: JSON.stringify({ username: name, nickname: 'x' }) });
  const reg2t = await reg2.text();
  ok('ثبت‌نام تکراری رد شد', reg2.status >= 400);
  ok('ارور تکراری JSON نیست', !reg2t.trim().startsWith('{"code"'), reg2t.slice(0, 80));

  // ۴) ورود با کد بازیابی
  const lg = await fetch(url + '/functions/v1/' + slug('login'), { method: 'POST', headers: { apikey: pub, 'Content-Type': 'application/json' }, body: JSON.stringify({ username: name, code: regj.recovery_code }) });
  const lgj = await lg.json();
  ok('ورود با کد بازیابی', lg.status === 200 && lgj.username === name);

  // ۵) دوئل با خود → باید ارور فارسی «تمیز» برگردد
  const duel = await fetch(url + '/rest/v1/rpc/create_duel', { method: 'POST', headers: H, body: JSON.stringify({ p_guest: name, p_duration_min: 15 }) });
  const duelt = await duel.text();
  ok('دوئل با خود رد شد', duel.status >= 400);
  let duelMsg = '';
  try { duelMsg = JSON.parse(duelt).message || JSON.parse(duelt).error || ''; } catch (_) {}
  ok('پیام دوئل فارسی و بدون کد', duelMsg.length > 0 && !/P0\d+|235/.test(duelMsg), duelMsg.slice(0, 60));

  // ۶) چالش روزانه — ۳ بار join → شمارنده ۱ می‌ماند
  for (let i = 0; i < 3; i++) await fetch(url + '/rest/v1/rpc/join_daily_challenge', { method: 'POST', headers: H, body: JSON.stringify({ p_goal: 'study', p_minutes: 30 }) });
  const daily = await fetch(url + '/rest/v1/rpc/daily_challenge', { method: 'POST', headers: H, body: '{}' }).then(r => r.json());
  ok('چالش: هر نفر فقط ۱ بار', daily[0] && daily[0].participants === 1, 'participants=' + (daily[0] && daily[0].participants));

  // ۷) لیگ — نباید کاربر الکی داشته باشد
  const league = await fetch(url + '/rest/v1/rpc/weekly_league', { method: 'POST', headers: H, body: '{}' }).then(r => r.json());
  const usernames = (league || []).map(r => r.username);
  ok('لیگ بدون کاربر الکی', !usernames.some(u => /alice\d+|bob\d+/.test(u)), JSON.stringify(usernames));

  // ۸) پاک‌سازی حساب تست
  const me = await fetch(url + '/rest/v1/profiles?username=eq.' + name + '&select=id', { headers: { apikey: key, Authorization: 'Bearer ' + key } }).then(r => r.json());
  if (me[0]) {
    await fetch(url + '/rest/v1/daily_challenge_joins?user_id=eq.' + me[0].id, { method: 'DELETE', headers: { apikey: key, Authorization: 'Bearer ' + key } });
    await fetch(url + '/rest/v1/profiles?id=eq.' + me[0].id, { method: 'DELETE', headers: { apikey: key, Authorization: 'Bearer ' + key } });
  }
  await fetch(url + '/rest/v1/credentials?username=eq.' + name, { method: 'DELETE', headers: { apikey: key, Authorization: 'Bearer ' + key } });
  const left = await fetch(url + '/rest/v1/profiles?select=username', { headers: { apikey: key, Authorization: 'Bearer ' + key } }).then(r => r.json());
  ok('پاک‌سازی تست (فقط abolfazl مانده)', left.length === 1 && left[0].username === 'abolfazl');

  console.log('\nنتیجه: ' + pass + ' سبز، ' + fail + ' قرمز');
  process.exit(fail > 0 ? 1 : 0);
})().catch(e => { console.error('خطای تست:', e.message); process.exit(1); });
