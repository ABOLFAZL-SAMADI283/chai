const PAT=process.argv[2],OWN="ABOLFAZL-SAMADI283",REPO="chai";
const H={Authorization:`Bearer ${PAT}`,Accept:"application/vnd.github+json","User-Agent":"s"};
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
async function get(u){for(let i=0;i<5;i++){try{const r=await fetch(u,{headers:H,signal:AbortSignal.timeout(20000)});return await r.json();}catch(e){if(i===4)throw e;await sleep(4000);}}}
(async()=>{
  for(let i=0;i<45;i++){
    const runs=await get(`https://api.github.com/repos/${OWN}/${REPO}/actions/workflows/android.yml/runs?per_page=1`);
    const run=(runs.workflow_runs||[])[0];
    if(!run){console.log("waiting...");await sleep(15000);continue;}
    console.log(`run ${run.id} → ${run.status}/${run.conclusion}`);
    if(run.status==="completed"){
      const jobs=await get(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs/${run.id}/jobs`);
      const j=jobs.jobs[0];
      if(run.conclusion==="failure"){
        const t=await(await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/jobs/${j.id}/logs`,{headers:H})).text();
        t.split("\n").forEach(l=>{ if(/\.kt:\d+/.test(l)) console.log("   "+l.replace(/^\S+Z /,"").slice(0,220)); });
      } else {
        (j.steps||[]).forEach(s=>console.log(`   ${s.conclusion} | ${s.name}`));
      }
      break;
    }
    await sleep(25000);
  }
})();
