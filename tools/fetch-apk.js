// دانلود آخرین APK از artifacts + ثبت درست secretها
const PAT = process.argv[2];
const REPO = 'chai';
const OWN = 'ABOLFAZL-SAMADI283';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'setup',
};
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

(async () => {
  // ── ۱) دانلود artifact دیباگ ──
  const runs = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/workflows/android.yml/runs?per_page=5`, { headers: H })).json();
  for (const run of (runs.workflow_runs || [])) {
    const arts = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs/${run.id}/artifacts`, { headers: H })).json();
    const art = (arts.artifacts || []).find(a => a.name === 'chai-debug-apk');
    if (!art) continue;
    console.log(`run ${run.id} → artifact ${art.id}`);
    const zip = await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/artifacts/${art.id}/zip`, { headers: H });
    const buf = Buffer.from(await zip.arrayBuffer());
    fs.writeFileSync('art.zip', buf);
    execSync('powershell -NoProfile -Command "Expand-Archive -Force -Path art.zip -DestinationPath art-out"');
    const apk = fs.readdirSync('art-out').find(f => f.endsWith('.apk'));
    if (apk) {
      const dest = path.join(process.env.USERPROFILE, 'Desktop', 'Chai');
      fs.mkdirSync(dest, { recursive: true });
      fs.copyFileSync(path.join('art-out', apk), path.join(dest, apk));
      console.log(`✔ APK saved: ${path.join(dest, apk)} (${(fs.statSync(path.join(dest, apk)).size / 1048576).toFixed(1)} MB)`);
    }
    fs.rmSync('art.zip', { force: true });
    fs.rmSync('art-out', { recursive: true, force: true });
    break;
  }

  // ── ۲) ثبت secretها (فرمت ساده: value + visibility) ──
  const b64 = fs.existsSync('keystore.b64') ? fs.readFileSync('keystore.b64', 'utf8').trim() : '';
  if (!b64) {
    console.log('\n(keystore.b64 پیدا نشد — از artifact قبلی باید بیاوریم)');
  } else {
    const secrets = [
      ['KEYSTORE_BASE64', b64],
      ['KEYSTORE_PASS', 'chai2026'],
      ['KEY_ALIAS', 'chai'],
      ['KEY_PASS', 'chai2026'],
    ];
    for (const [name, value] of secrets) {
      const r = await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/secrets/${name}`, {
        method: 'PUT',
        headers: { ...H, 'Content-Type': 'application/json' },
        body: JSON.stringify({ value, visibility: 'all' }),
      });
      const t = await r.text();
      console.log(`  secret ${name}: ${r.status} ${r.ok ? '✔' : t.slice(0, 160)}`);
    }
  }
})();
