// وضعیت آخرین اجرای workflow (با تلاش مجدد برای خطای شبکه)
const PAT = process.argv[2];
const OWN = 'ABOLFAZL-SAMADI283';
const REPO = 'chai';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'check',
};
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

async function get(url) {
  for (let i = 0; i < 6; i++) {
    try {
      const r = await fetch(url, { headers: H, signal: AbortSignal.timeout(20000) });
      return await r.json();
    } catch (e) {
      if (i === 5) throw e;
      await sleep(3000 * (i + 1));
    }
  }
}

(async () => {
  const wf = process.argv[3] || 'android.yml';
  const runs = await get(`https://api.github.com/repos/${OWN}/${REPO}/actions/workflows/${wf}/runs?per_page=3`);
  for (const run of runs.workflow_runs || []) {
    console.log(`run ${run.id} | ${run.status} | ${run.conclusion} | ${run.head_commit?.message?.slice(0, 60)} | ${run.html_url}`);
    if (run.status === 'completed') {
      const arts = await get(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs/${run.id}/artifacts`);
      for (const a of arts.artifacts || []) {
        console.log(`   artifact: ${a.name} (${(a.size_in_bytes / 1048576).toFixed(1)} MB, id ${a.id})`);
      }
    }
  }
})();
