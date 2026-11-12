// صبر کردن تا پایان بیلد و چاپ لاگ خطاها
const PAT = process.argv[2];
const REPO = 'chai';
const OWN = 'ABOLFAZL-SAMADI283';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'watch',
};
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

(async () => {
  const runs = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs?per_page=5`, { headers: H })).json();
  const run = (runs.workflow_runs || [])[0];
  console.log(`watching run ${run.id} (${run.html_url})`);

  for (let i = 0; i < 60; i++) {
    await sleep(20000);
    const r = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs/${run.id}`, { headers: H })).json();
    const jobs = await (await fetch(r.jobs_url, { headers: H })).json();
    const job = (jobs.jobs || [])[0];
    const step = job ? (job.steps || []).filter(s => s.status === 'in_progress' || s.conclusion === 'failure').map(s => s.name).join(',') : '';
    console.log(`  [${new Date().toISOString().slice(11, 19)}] ${r.status}/${r.conclusion}${step ? ' | ' + step : ''}`);
    if (r.status === 'completed') {
      console.log(`\n=== نتیجه: ${r.conclusion} ===`);
      if (job) {
        (job.steps || []).forEach(s => console.log(`  • ${s.name}: ${s.conclusion}`));
        const log = await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/jobs/${job.id}/logs`, { headers: H });
        if (log.ok) {
          const text = await log.text();
          const lines = text.split('\n');
          const err = lines.filter(l => /error:|e: |FAILURE|What went wrong|Could not|Execution failed|Unresolved reference|AAPT|Manifest merger/i.test(l));
          console.log('\n──── خط‌های خطا ────');
          console.log(err.slice(0, 40).join('\n') || '(خطای مشخصی نبود)');
          console.log('\n──── ۳۵ خط آخر ────');
          console.log(lines.slice(-35).join('\n'));
        }
      }
      process.exit(0);
    }
  }
  console.log('timeout');
})();
