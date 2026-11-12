// دانلود artifact مشخص‌شده از آخرین اجرای موفق
const PAT = process.argv[2];
const OWN = 'ABOLFAZL-SAMADI283';
const REPO = 'chai';
const ART = process.argv[3] || 'chai-release-apk';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'fetch',
};
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

async function get(url, raw = false) {
  for (let i = 0; i < 5; i++) {
    try {
      const r = await fetch(url, { headers: H, signal: AbortSignal.timeout(raw ? 120000 : 30000) });
      if (raw) return r;
      return await r.json();
    } catch (e) {
      if (i === 4) throw e;
      await sleep(5000 * (i + 1));
    }
  }
}

(async () => {
  const runs = await get(`https://api.github.com/repos/${OWN}/${REPO}/actions/workflows/android.yml/runs?per_page=6`);
  const run = (runs.workflow_runs || []).find(r => r.status === 'completed' && r.conclusion === 'success');
  if (!run) { console.log('no successful run'); process.exit(1); }
  console.log(`run ${run.id} (${run.head_commit?.message?.slice(0, 50)})`);

  const arts = await get(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs/${run.id}/artifacts`);
  const art = (arts.artifacts || []).find(a => a.name === ART);
  if (!art) { console.log(`artifact ${ART} not found`); process.exit(1); }

  const zip = await get(`https://api.github.com/repos/${OWN}/${REPO}/actions/artifacts/${art.id}/zip`, true);
  const buf = Buffer.from(await zip.arrayBuffer());
  fs.writeFileSync('art.zip', buf);
  execSync('powershell -NoProfile -Command "Expand-Archive -Force -Path art.zip -DestinationPath art-out"');

  const dest = path.join(process.env.USERPROFILE, 'Desktop', 'Chai');
  fs.mkdirSync(dest, { recursive: true });
  for (const f of fs.readdirSync('art-out')) {
    if (!f.endsWith('.apk')) continue;
    const target = path.join(dest, f.replace(/\.apk$/, '-release.apk'));
    fs.copyFileSync(path.join('art-out', f), target);
    console.log(`✔ ${target} (${(fs.statSync(target).size / 1048576).toFixed(1)} MB)`);
  }
  fs.rmSync('art.zip', { force: true });
  fs.rmSync('art-out', { recursive: true, force: true });
})();
