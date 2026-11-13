// انتشار صفحات قانونی روی Supabase Edge Functions (privacy-policy و terms)
// استفاده: node tools\deploy-legal.js <SUPABASE_PAT> [projectRef]
const fs = require('fs');
const path = require('path');

const PAT = process.argv[2];
const REF = process.argv[3] || 'jgeqpxhkltxtcbccywft';
const ROOT = path.join(__dirname, '..');
const API = 'https://api.supabase.com';

const PAGES = [
  { fn: 'privacy-policy', file: 'privacy-policy.html' },
  { fn: 'terms', file: 'terms.html' },
];

if (!PAT) {
  console.error('usage: node deploy-legal.js <PERSONAL_ACCESS_TOKEN> [projectRef]');
  process.exit(1);
}

(async () => {
  for (const p of PAGES) {
    const html = fs.readFileSync(path.join(ROOT, 'store', p.file), 'utf8');
    const tpl = fs.readFileSync(
      path.join(ROOT, 'supabase', 'functions', 'privacy-policy', 'index.ts'), 'utf8'
    );
    const body = tpl.replace('__PRIVACY_HTML__', html);

    // ساخت یا به‌روزرسانی تابع
    let r = await fetch(`${API}/v1/projects/${REF}/functions`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${PAT}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        slug: p.fn, name: p.fn, body,
        entrypoint_path: 'index.ts', verify_jwt: false,
      }),
    });
    let text = await r.text();
    if (!r.ok) {
      // اگر از قبل وجود داشت، با PATCH به‌روزرسانی می‌کنیم
      r = await fetch(`${API}/v1/projects/${REF}/functions/${p.fn}`, {
        method: 'PATCH',
        headers: { Authorization: `Bearer ${PAT}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: p.fn, body, entrypoint_path: 'index.ts', verify_jwt: false,
        }),
      });
      text = await r.text();
    }
    console.log(`${r.ok ? '✔' : '✘'} ${p.fn}: ${r.status} ${r.ok ? '' : text.slice(0, 200)}`);
  }

  // همگام‌سازی مسیرها
  const list = await (await fetch(`${API}/v1/projects/${REF}/functions`, {
    headers: { Authorization: `Bearer ${PAT}` },
  })).json();
  if (Array.isArray(list)) {
    const rows = list.map(f => `('${f.name}','${f.slug}')`).join(',');
    const up = await (await fetch(`${API}/v1/projects/${REF}/database/query`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${PAT}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: `insert into public.function_routes (name, slug) values ${rows} on conflict (name) do update set slug = excluded.slug, updated_at = now();`,
      }),
    })).json();
    console.log(up ? '✔ routes synced' : '✘ routes sync failed');
  }

  // تست نهایی
  for (const p of PAGES) {
    const url = `https://${REF}.supabase.co/functions/v1/${p.fn}`;
    const r = await fetch(url);
    const t = await r.text();
    console.log(`  ${url} → HTTP ${r.status} · ${t.length} bytes`);
  }
})();
