const PAT=process.argv[2],REPO="chai",OWN="ABOLFAZL-SAMADI283";
const H={Authorization:`Bearer ${PAT}`,Accept:"application/vnd.github+json","X-GitHub-Api-Version":"2022-11-28","User-Agent":"diag","Content-Type":"application/json"};
(async()=>{
  const p=await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/permissions`,{headers:H});
  console.log("permissions →",p.status,(await p.text()).slice(0,300));
  const e=await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/permissions/workflow`,{headers:H});
  console.log("workflow permissions →",e.status,(await e.text()).slice(0,200));
  // اجرای دستی
  const d=await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/workflows/android.yml/dispatches`,{method:"POST",headers:H,body:JSON.stringify({ref:"main"})});
  console.log("dispatch →",d.status,(await d.text()).slice(0,200));
  await new Promise(r=>setTimeout(r,8000));
  const runs=await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/runs?per_page=5`,{headers:H})).json();
  (runs.workflow_runs||[]).forEach(r=>console.log(`  run ${r.id}: ${r.status}/${r.conclusion}  jobs=${r.run_attempt}  ${r.created_at}`));
  const last=(runs.workflow_runs||[])[0];
  if(last){const j=await (await fetch(last.jobs_url,{headers:H})).json();
    console.log("  jobs:",(j.jobs||[]).length);
    (j.jobs||[]).forEach(job=>{console.log(`   ${job.name} → ${job.conclusion}`);(job.steps||[]).forEach(s=>console.log(`     • ${s.name}: ${s.conclusion}`));});}
})();
