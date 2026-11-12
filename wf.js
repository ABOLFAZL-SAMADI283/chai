const PAT=process.argv[2],REPO="chai";
const H={Authorization:`Bearer ${PAT}`,Accept:"application/vnd.github+json","X-GitHub-Api-Version":"2022-11-28","User-Agent":"diag"};
(async()=>{
  const wf=await (await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/actions/workflows`,{headers:H})).json();
  console.log("workflows:",JSON.stringify((wf.workflows||[]).map(w=>({id:w.id,name:w.name,path:w.path,state:w.state}))));
  const run=await (await fetch(`https://api.github.com/repos/ABOLFAZL-SAMADI283/${REPO}/actions/runs/43273083282`,{headers:H})).json();
  console.log("run:",run.status,run.conclusion,"| path:",run.path,"| event:",run.event);
  console.log("display_title:",run.display_title);
  console.log("--- raw workflow ---");
  const raw=await (await fetch(`https://raw.githubusercontent.com/ABOLFAZL-SAMADI283/${REPO}/main/.github/workflows/android.yml`,{headers:{Accept:"application/vnd.github.raw"}})).text();
  console.log(raw.split("\n").slice(0,14).join("\n"));
  console.log("...");
  console.log("CRLF count:",(raw.match(/\r\n/g)||[]).length);
})();
