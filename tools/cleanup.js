// پاکسازی کاربران تستی + گزارش وضعیت دیتابیس
const PAT = process.argv[2];
const REF = 'jgeqpxhkltxtcbccywft';
const h = { Authorization: `Bearer ${PAT}`, 'Content-Type': 'application/json' };

(async () => {
  const q = await fetch(`https://api.supabase.com/v1/projects/${REF}/database/query`, {
    method: 'POST', headers: h,
    body: JSON.stringify({
      query: `delete from public.credentials where username like 'tester%' or username like 'slugtest%';
               delete from public.profiles where username like 'tester%' or username like 'slugtest%';
               delete from public.active_sessions;`
    }),
  });
  console.log('cleanup:', q.ok ? '✔' : '✘');

  const r = await fetch(`https://api.supabase.com/v1/projects/${REF}/database/query`, {
    method: 'POST', headers: h,
    body: JSON.stringify({
      query: `select (select count(*) from public.profiles) profiles,
                     (select count(*) from public.credentials) creds,
                     (select count(*) from public.function_routes) routes,
                     (select count(*) from public.active_sessions) active,
                     (select count(*) from information_schema.tables where table_schema='public') tables,
                     (select count(*) from information_schema.routines where routine_schema='public') functions;`
    }),
  });
  console.log('state:', (await r.text()).trim());
})();
