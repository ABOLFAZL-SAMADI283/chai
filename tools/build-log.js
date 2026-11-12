// خواندن لاگ آخرین بیلد گیت‌هاب اکشنز
const PAT = process.argv[2];
const REPO = process.argv[3] || 'chai';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'diag',
};

(async () => {
  const runs = await (await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/actions/runs?per_page=3`, { headers: H })).json();
  const run = (runs.workflow_runs || [])[0];
  if (!run) { console.log('هیچ بیلدی نیست'); return; }
  console.log(`run: ${run.id} | ${run.status} / ${run.conclusion}`);
  console.log(`url: ${run.html_url}\n`);

  const jobs = await (await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/actions/runs/${run.id}/jobs`, { headers: H })).json();
  for (const job of (jobs.jobs || [])) {
    console.log(`job: ${job.name} → ${job.conclusion}`);
    for (const step of (job.steps || [])) {
      console.log(`   • ${step.name}: ${step.conclusion}`);
    }
    // لاگ همان job
    const log = await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/actions/jobs/${job.id}/logs`, { headers: H });
    if (log.ok) {
      const text = await log.text();
      const lines = text.split('\n');
      const interesting = lines.filter(l =>
        /error|Error|ERROR|e: |FAILURE|What went wrong|Caused by|Unresolved|warning: |Could not|failed/i.test(l)
      );
      console.log('\n──── خط‌های مهم ────');
      console.log(interesting.slice(-60).join('\n'));
      console.log('\n──── ۴۰ خط آخر ────');
      console.log(lines.slice(-40).join('\n'));
    }
  }
})();
