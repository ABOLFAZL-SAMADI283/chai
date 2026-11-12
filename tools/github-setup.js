// راه‌اندازی گیت‌هاب: ساخت ریپو + push + فعال‌سازی Actions + گزارش وضعیت بیلد
// استفاده: node github-setup.js <GITHUB_PAT> [repoName]
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const PAT = process.argv[2];
const REPO = process.argv[3] || 'chai';
const ROOT = path.join(__dirname, '..');
const API = 'https://api.github.com';

if (!PAT) {
  console.error('usage: node github-setup.js <GITHUB_PAT> [repoName]');
  process.exit(1);
}

const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'chai-setup',
};

async function api(method, url, body) {
  const r = await fetch(`${API}${url}`, {
    method,
    headers: { ...H, ...(body ? { 'Content-Type': 'application/json' } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  const t = await r.text();
  let j = null; try { j = JSON.parse(t); } catch (_) {}
  return { ok: r.ok, status: r.status, json: j, text: t };
}

const git = (cmd) => execSync(cmd, { cwd: ROOT, stdio: 'pipe' }).toString().trim();

(async () => {
  // ۱) هویت
  const me = await api('GET', '/user');
  if (!me.ok) { console.error('✘ توکن نامعتبر:', me.text.slice(0, 200)); process.exit(1); }
  const user = me.json.login;
  console.log(`✔ کاربر: ${user}`);

  // ۲) ساخت ریپو (اگر نیست)
  const chk = await api('GET', `/repos/${user}/${REPO}`);
  if (!chk.ok) {
    const cr = await api('POST', '/user/repos', {
      name: REPO,
      description: 'چای — اپ تمرکز فارسی',
      private: false,
      auto_init: false,
    });
    if (!cr.ok) { console.error('✘ ساخت ریپو:', cr.text.slice(0, 300)); process.exit(1); }
    console.log(`✔ ریپو ساخته شد: ${user}/${REPO}`);
  } else {
    console.log(`✔ ریپو از قبل هست: ${user}/${REPO}`);
  }

  // ۳) ریموت (توکن ذخیره نمی‌شود)
  const remote = `https://github.com/${user}/${REPO}.git`;
  try { git(`git remote remove origin`); } catch (_) {}
  git(`git remote add origin ${remote}`);
  console.log(`✔ remote: ${remote}`);

  // ۴) push با هدر احراز هویت (بدون ذخیرهٔ توکن روی دیسک)
  const basic = Buffer.from(`x-access-token:${PAT}`).toString('base64');
  try {
    git(`git -c http.extraheader="AUTHORIZATION: basic ${basic}" push -u origin HEAD:main`);
    console.log('✔ push انجام شد');
  } catch (e) {
    console.error('✘ push failed:', (e.stdout || '').toString().slice(-400) || e.message);
    process.exit(1);
  }

  // ۵) وضعیت بیلد
  await new Promise(r => setTimeout(r, 6000));
  const runs = await api('GET', `/repos/${user}/${REPO}/actions/runs?per_page=3`);
  const list = (runs.json && runs.json.workflow_runs) || [];
  if (list.length) {
    const r0 = list[0];
    console.log(`\n▸ بیلد: ${r0.status} / ${r0.conclusion || 'در حال اجرا'}`);
    console.log(`  ${r0.html_url}`);
  } else {
    console.log('\n▸ بیلدی هنوز شروع نشده — چند ثانیه بعد صفحهٔ Actions را باز کن');
  }
  console.log(`\n✔ همه‌چیز آماده است: https://github.com/${user}/${REPO}/actions`);
})();
