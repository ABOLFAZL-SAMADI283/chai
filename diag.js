const PAT=process.argv[2];
const H={Authorization:`Bearer ${PAT}`,Accept:"application/vnd.github+json","X-GitHub-Api-Version":"2022-11-28","User-Agent":"diag"};
(async()=>{
  const u=await (await fetch("https://api.github.com/user",{headers:H})).json();
  console.log("user:",u.login,"type:",u.type);
  const r=await fetch(`https://api.github.com/repos/${u.login}/chai`,{headers:H});
  const rj=await r.json();
  console.log("GET repo →",r.status,"| visibility:",rj.visibility,"| perms:",JSON.stringify(rj.permissions));
  const t=await fetch("https://api.github.com/user",{headers:H});
  console.log("token scopes header:",t.headers.get("x-oauth-scopes")||"(fine-grained: ندارد)");
  // تست دسترسی نوشتن روی یک فایل آزمایشی
  const w=await fetch(`https://api.github.com/repos/${u.login}/chai/contents/.github/probe.txt`,{method:"PUT",headers:{...H,"Content-Type":"application/json"},body:JSON.stringify({message:"probe",content:Buffer.from("probe").toString("base64")})});
  console.log("PUT file →",w.status,(await w.text()).slice(0,160));
})();
