// ری‌لود کش PostgREST
const fs = require('fs');
const path = require('path');
const s = JSON.parse(fs.readFileSync(path.join(process.env.HOME || 'C:/Users/Abolfazl Samadi/.config/opencode', '.config/opencode/secrets.json'), 'utf8').length ? fs.readFileSync('C:/Users/Abolfazl Samadi/.config/opencode/secrets.json', 'utf8') : '{}');
const PAT = process.argv[2] || s.SUPABASE_PAT;
(async () => {
  const r = await fetch('https://api.supabase.com/v1/projects/jgeqpxhkltxtcbccywft/database/query', {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + PAT, 'Content-Type': 'application/json' },
    body: JSON.stringify({ query: "NOTIFY pgrst, 'reload schema';" }),
  });
  console.log(r.status, (await r.text()).slice(0, 300));
})();
