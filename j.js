const PAT=process.argv[2],REPO="chai";
const H={Authorization:`Bearer ${PAT}`,Accept:"application/vnd.github+json","X-GitHub-Api-Version":"2022-11-28","User-Agent":"diag"};
(async()=>{
  const r=await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/actions/runs/43273083282/jobs`,{headers:H});
  const t=await r.text();
  console.log("jobs status:",r.status);
  console.log(t.slice(0,900));
})();
