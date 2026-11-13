// اجرای یک فایل SQL روی پروژهٔ Supabase از طریق Management API
// استفاده: node tools\run-sql.js <PAT> <file.sql>
const fs = require('fs');

const PAT = process.argv[2];
const FILE = process.argv[3];
const REF = process.argv[4] || 'jgeqpxhkltxtcbccywft';

if (!PAT || !FILE) {
  console.error('usage: node run-sql.js <PAT> <file.sql> [projectRef]');
  process.exit(1);
}

(async () => {
  const sql = fs.readFileSync(FILE, 'utf8');
  const r = await fetch(`https://api.supabase.com/v1/projects/${REF}/database/query`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${PAT}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ query: sql }),
  });
  const t = await r.text();
  console.log(r.ok ? '✔ SQL اجرا شد' : '✘ خطا: ' + t.slice(0, 800));
  if (r.ok) {
    let j = null; try { j = JSON.parse(t); } catch (_) {}
    if (j) console.log(JSON.stringify(j).slice(0, 600));
  }
})();
