// دانلود keystore از artifact و ثبت آن به‌عنوان secret در ریپو
const PAT = process.argv[2];
const REPO = 'chai';
const OWN = 'ABOLFAZL-SAMADI283';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'setup',
};
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

(async () => {
  // ۱) صبر تا کامل شدن اجرای keystore
  let run = null;
  for (let i = 0; i < 40; i++) {
    const runs = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/workflows/keystore.yml/runs?per_page=1`, { headers: H })).json();
    run = (runs.workflow_runs || [])[0];
    if (run && run.status === 'completed') break;
    console.log('  waiting…', run ? run.status : 'no run');
    await sleep(15000);
  }
  if (!run || run.status !== 'completed') { console.log('keystore run did not finish'); process.exit(1); }
  console.log(`keystore run: ${run.conclusion}`);

  // ۲) دانلود artifact
  const arts = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs/${run.id}/artifacts`, { headers: H })).json();
  const art = (arts.artifacts || []).find(a => a.name === 'chai-keystore');
  if (!art) { console.log('artifact not found'); process.exit(1); }

  const zipRes = await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/artifacts/${art.id}/zip`, { headers: H });
  const buf = Buffer.from(await zipRes.arrayBuffer());
  require('fs').writeFileSync('keystore.zip', buf);
  console.log(`artifact downloaded: ${(buf.length / 1024).toFixed(1)} KB`);

  // ۳) استخراج فایل base64
  const { execSync } = require('child_process');
  execSync('powershell -NoProfile -Command "Expand-Archive -Force -Path keystore.zip -DestinationPath keystore-out"');
  const b64 = require('fs').readFileSync('keystore-out/release.jks.base64', 'utf8').trim();
  const sha = require('fs').readFileSync('keystore-out/release.jks.sha256', 'utf8').trim();
  console.log('sha256:', sha);

  // ۴) ثبت secrets
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
      body: JSON.stringify({ encrypted_value: null, key_id: null, value, visibility: 'all' }),
    });
    console.log(`  secret ${name}: ${r.status}`);
  }

  // ۵) پاکسازی فایل‌های محلی
  require('fs').rmSync('keystore.zip', { force: true });
  require('fs').rmSync('keystore-out', { recursive: true, force: true });
  console.log('\n✔ secrets ثبت شد — حالا بیلد ریلیز امضاشده می‌سازه');
})();
