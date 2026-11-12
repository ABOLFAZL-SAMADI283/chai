const PAT=process.argv[2],REPO="chai";
const H={Authorization:`Bearer ${PAT}`,Accept:"application/vnd.github+json","X-GitHub-Api-Version":"2022-11-28","User-Agent":"diag"};
(async()=>{
  const run=await (await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/actions/runs/43273083282`,{headers:H})).json();
  console.log("--- run fields ---");
  ["status","conclusion","run_attempt","created_at","updated_at","run_started_at","jobs_url","check_suite_id","head_sha","path","event"].forEach(k=>console.log(" ",k,"=",run[k]));
  const cs=await (await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/commits/${run.head_sha}/check-runs`,{headers:H})).json();
  console.log("--- check runs:",(cs.check_runs||[]).length);
  for(const c of (cs.check_runs||[])){
    console.log(`  ${c.name} → ${c.conclusion}`);
    console.log("  output:",JSON.stringify(c.output||{}).slice(0,900));
  }
})();
