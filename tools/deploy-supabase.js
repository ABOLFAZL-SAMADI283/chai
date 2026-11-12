// استقرار کامل روی Supabase با Personal Access Token:
//   ۱) اجرای db/schema.sql
//   ۲) استقرار توابع Edge: register · login · check-username
//   ۳) تست نهایی
//
// استفاده:  node deploy-supabase.js <PAT> [projectRef]
const fs = require('fs');
const path = require('path');

const PAT = process.argv[2];
const REF = process.argv[3] || 'jgeqpxhkltxtcbccywft';
const ROOT = path.join(__dirname, '..');
const API = 'https://api.supabase.com';

if (!PAT) {
  console.error('usage: node deploy-supabase.js <PERSONAL_ACCESS_TOKEN> [projectRef]');
  process.exit(1);
}

const headers = {
  Authorization: `Bearer ${PAT}`,
  'Content-Type': 'application/json',
};

async function api(method, endpoint, body) {
  const res = await fetch(`${API}${endpoint}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json = null;
  try { json = JSON.parse(text); } catch (_) {}
  return { ok: res.ok, status: res.status, json, text };
}

/** فایل تابع + util مشترک، به‌صورت یک فایل مستقل */
function buildFunction(folder) {
  const shared = fs.readFileSync(path.join(ROOT, 'supabase', 'functions', '_shared', 'util.ts'), 'utf8');
  const fn = fs.readFileSync(path.join(ROOT, 'supabase', 'functions', folder, 'index.ts'), 'utf8');
  const withoutImport = fn.replace(/^import\s*\{[^}]*\}\s*from\s*"\.\.\/_shared\/util\.ts";?/m, '');
  return `${shared}\n\n// ── تابع ─────────────────────────────────────────────\n${withoutImport}`;
}

(async () => {
  // ── ۱) دیتابیس ──
  console.log('▸ applying schema…');
  const sql = fs.readFileSync(path.join(ROOT, 'db', 'schema.sql'), 'utf8');
  const db = await api('POST', `/v1/projects/${REF}/database/query`, { query: sql });
  if (!db.ok) {
    console.error('  ✘ schema failed:', db.text.slice(0, 500));
    process.exit(1);
  }
  console.log('  ✔ schema applied');

  // ── ۲) توابع ──
  for (const fn of ['register', 'login', 'check-username']) {
    console.log(`▸ deploying ${fn}…`);
    const body = buildFunction(fn);
    const fd = new FormData();
    fd.append('metadata', JSON.stringify({
      name: fn,
      entrypoint_path: 'index.ts',
      verify_jwt: false,
    }));
    fd.append('file', new Blob([body], { type: 'application/typescript' }), 'index.ts');
    // slug در مسیر قرار می‌گیرد تا آدرس نهایی /functions/v1/<name> باشد
    const res = await fetch(`${API}/v1/projects/${REF}/functions/${fn}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${PAT}` },   // FormData خودش boundary می‌گذارد
      body: fd,
    });
    const text = await res.text();
    if (!res.ok) {
      console.error(`  ✘ ${fn}:`, text.slice(0, 400));
    } else {
      console.log(`  ✔ ${fn} deployed`);
    }
  }

  // ── ۳) مسیر توابع (نام → slug) برای اپ ──
  console.log('▸ syncing function routes…');
  const list = await api('GET', `/v1/projects/${REF}/functions`);
  if (list.ok && Array.isArray(list.json)) {
    const rows = list.json.map(f => ({ name: f.name, slug: f.slug }));
    const sqlRows = rows.map(r => `('${r.name}','${r.slug}')`).join(',');
    const up = await api('POST', `/v1/projects/${REF}/database/query`, {
      query: `insert into public.function_routes (name, slug) values ${sqlRows} on conflict (name) do update set slug = excluded.slug, updated_at = now();`,
    });
    console.log(up.ok ? '  ✔ routes synced: ' + rows.map(r => `${r.name}→${r.slug.slice(0, 8)}…`).join(', ') : '  ✘ ' + up.text.slice(0, 200));
  }

  // ── ۴) تست ──
  console.log('▸ testing…');
  const url = `https://${REF}.supabase.co`;
  const pub = JSON.parse(
    fs.readFileSync(path.join(process.env.USERPROFILE, '.config', 'opencode', 'secrets.json'), 'utf8')
      .replace(/^\uFEFF/, '')
  );
  const r = await fetch(`${url}/rest/v1/profiles?select=id&limit=1`, {
    headers: { apikey: pub.SUPABASE_PUBLISHABLE_KEY, Authorization: `Bearer ${pub.SUPABASE_PUBLISHABLE_KEY}` },
  });
  console.log(`  tables reachable: HTTP ${r.status}`);
  const rr = await fetch(`${url}/rest/v1/rpc/active_count`, {
    method: 'POST',
    headers: { apikey: pub.SUPABASE_SECRET_KEY, Authorization: `Bearer ${pub.SUPABASE_SECRET_KEY}`, 'Content-Type': 'application/json' },
    body: '{}',
  });
  console.log(`  active_count(): HTTP ${rr.status} → ${(await rr.text()).slice(0, 120)}`);
  console.log('ALL DONE ✔');
})();
