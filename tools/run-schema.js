// اجرای db/schema.sql روی پروژهٔ Supabase از طریق Connection Pooler
// استفاده: node run-schema.js <DB_PASSWORD> [schemaPath]
const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

const REF = 'jgeqpxhkltxtcbccywft.supabase.co'.split('.')[0];
const REGIONS = [
  'aws-0-ap-northeast-1.pooler.supabase.com',   // Tokyo
  'aws-0-ap-southeast-1.pooler.supabase.com',   // Singapore
  'aws-0-ap-southeast-2.pooler.supabase.com',   // Jakarta
  'aws-0-us-east-1.pooler.supabase.com',        // Virginia
];

const password = process.argv[2];
const schemaPath = process.argv[3] || path.join(__dirname, '..', 'db', 'schema.sql');

if (!password) {
  console.error('usage: node run-schema.js <DB_PASSWORD> [schemaPath]');
  process.exit(1);
}

const sql = fs.readFileSync(schemaPath, 'utf8');

(async () => {
  for (const host of REGIONS) {
    const conn = `postgresql://postgres.${REF}:${encodeURIComponent(password)}@${host}:5432/postgres`;
    const client = new Client({ connectionString: conn, ssl: { rejectUnauthorized: false }, connectionTimeoutMillis: 12000 });
    try {
      await client.connect();
      console.log(`connected: ${host}`);
      await client.query(sql);
      console.log('schema applied ✔');
      // گزارش جدول‌ها
      const r = await client.query(
        `select table_name from information_schema.tables where table_schema='public' order by table_name;`
      );
      console.log('tables:', r.rows.map(x => x.table_name).join(', '));
      const f = await client.query(
        `select routine_name from information_schema.routines where routine_schema='public' order by routine_name;`
      );
      console.log('functions:', f.rows.map(x => x.routine_name).join(', '));
      await client.end();
      process.exit(0);
    } catch (e) {
      console.log(`  ✘ ${host}: ${e.message}`);
      try { await client.end(); } catch (_) {}
    }
  }
  console.error('اتصال به هیچ regionای برقرار نشد — رمز دیتابیس را چک کن');
  process.exit(1);
})();
