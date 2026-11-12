// انتشار تابع Edge «privacy-policy» و همگام‌سازی function_routes
// استفاده: node tools\deploy-privacy.js <SUPABASE_PAT> [projectRef]
const fs = require('fs');
const path = require('path');

const PAT = process.argv[2];
const REF = process.argv[3] || 'jgeqpxhkltxtcbccywft';
const ROOT = path.join(__dirname, '..');
const API = 'https://api.supabase.com';

if (!PAT) {
  console.error('usage: node deploy-privacy.js <PERSONAL_ACCESS_TOKEN> [projectRef]');
  process.exit(1);
}

(async () => {
  const html = fs.readFileSync(path.join(ROOT, 'store', 'privacy-policy.html'), 'utf8');
  const tpl = fs.readFileSync(path.join(ROOT, 'supabase', 'functions', 'privacy-policy', 'index.ts'), 'utf8');
  const body = tpl.replace('__PRIVACY_HTML__', html);

  const fd = new FormData();
  fd.append('metadata', JSON.stringify({ name: 'privacy-policy', entrypoint_path: 'index.ts', verify_jwt: false }));
  fd.append('file', new Blob([body], { type: 'application/typescript' }), 'index.ts');

  const res = await fetch(`${API}/v1/projects/${REF}/functions/privacy-policy`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${PAT}` },
    body: fd,
  });
  const text = await res.text();
  if (!res.ok) {
    console.error('✘ deploy failed:', text.slice(0, 500));
    process.exit(1);
  }
  console.log('✔ privacy-policy deployed');

  // همگام‌سازی جدول function_routes (نام → اسلاگ)
  const list = await (await fetch(`${API}/v1/projects/${REF}/functions`, {
    headers: { Authorization: `Bearer ${PAT}` },
  })).json();
  if (Array.isArray(list)) {
    const rows = list.map(f => `('${f.name}','${f.slug}')`).join(',');
    const up = await (await fetch(`${API}/v1/projects/${REF}/database/query`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${PAT}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: `insert into public.function_routes (name, slug) values ${rows} on conflict (name) do update set slug = excluded.slug, updated_at = now();` }),
    })).json();
    console.log(up ? '✔ routes synced' : '✘ routes sync failed');
  }

  // تست نهایی
  const slug = (list.find(f => f.name === 'privacy-policy') || {}).slug;
  const url = slug
    ? `https://${REF}.supabase.co/functions/v1/${slug}`
    : `https://${REF}.supabase.co/functions/v1/privacy-policy`;
  const t = await fetch(url);
  const html2 = await t.text();
  console.log(`\n✔ URL سیاست حریم خصوصی:\n  ${url}`);
  console.log(`  HTTP ${t.status} · ${t.headers.get('content-type')} · ${html2.length} بایت`);
})();
