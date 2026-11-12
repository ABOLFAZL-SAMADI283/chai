// تست کامل جریان: چک نام کاربری → ثبت‌نام → ورود → بازدید → شمارندهٔ فعال → لیگ
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

async function post(p, body, key = PUB) {
  const r = await fetch(`${URL}${p}`, {
    method: 'POST',
    headers: { apikey: key, Authorization: `Bearer ${key}`, 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const t = await r.text();
  let j = null; try { j = JSON.parse(t); } catch (_) {}
  return { status: r.status, json: j, text: t };
}
async function get(p, key = PUB) {
  const r = await fetch(`${URL}${p}`, { headers: { apikey: key, Authorization: `Bearer ${key}` } });
  const t = await r.text();
  let j = null; try { j = JSON.parse(t); } catch (_) {}
  return { status: r.status, json: j, text: t };
}

(async () => {
  const user = 'tester' + Date.now().toString().slice(-6);
  console.log(`\n▸ کاربر آزمایشی: ${user}`);

  // مسیر توابع (نام → slug)
  const rt = await get('/rest/v1/function_routes?select=name,slug', SECRET);
  const R = {};
  (rt.json || []).forEach(x => { R[x.name] = x.slug; });
  console.log('▸ routes:', JSON.stringify(R));

  console.log('\n۱) check-username (نام آزاد + پیشنهاد)');
  const cu = await get(`/functions/v1/${R['check-username']}?name=${user}&suggest=1`);
  console.log('   →', cu.status, cu.text.slice(0, 260));

  console.log('\n۲) register');
  const reg = await post(`${FN}/${R.register}`, { username: user, nickname: 'تستر' });
  console.log('   status:', reg.status);
  if (reg.status !== 200) { console.log('   →', reg.text.slice(0, 400)); process.exit(1); }
  const recovery = reg.json.recovery_code;
  console.log('   کد بازیابی:', recovery);
  console.log('   توکن:', String(reg.json.access_token).slice(0, 30) + '…');

  console.log('\n۳) login');
  const bad = await post(`${FN}/${R.login}`, { username: user, code: 'WRONG-CODE' });
  console.log('   کد غلط →', bad.status, bad.json && bad.json.error);
  const log = await post(`${FN}/${R.login}`, { username: user, code: recovery });
  console.log('   کد درست →', log.status, log.json && log.json.access_token ? 'توکن گرفت ✅' : log.text.slice(0, 200));

  console.log('\n۴) ثبت بازدید (log_visit)');
  const lv = await post('/rest/v1/rpc/log_visit', { p_anon: 'test-' + user, p_goal: 'study' }, SECRET);
  console.log('   →', lv.status, JSON.stringify(lv.json));

  console.log('\n۵) شمارندهٔ کاربران فعال');
  const ac = await post('/rest/v1/rpc/active_count', {}, SECRET);
  console.log('   →', JSON.stringify(ac.json));

  console.log('\n۶) لیگ هفتگی');
  const wl = await post('/rest/v1/rpc/weekly_league', {}, SECRET);
  console.log('   →', JSON.stringify(wl.json).slice(0, 240));

  console.log('\n۷) RLS — با کلید publishable (نقش anon) چی می‌بینیم؟');
  const prof = await get('/rest/v1/profiles?select=username,nickname,total_sec');
  console.log('   →', prof.status, JSON.stringify(prof.json).slice(0, 240));

  console.log('\n۸) سشن‌ها — با توکن واقعی کاربر');
  const sess = await fetch(`${URL}/rest/v1/sessions?select=*`, { headers: { apikey: PUB, Authorization: `Bearer ${(log.json && log.json.access_token) || PUB}` } }).then(async r => ({ status: r.status, json: await r.json() }));
  console.log('   →', sess.status, JSON.stringify(sess.json).slice(0, 240));

  console.log('\n✔ تمام تست‌ها اجرا شد\n');
})();
