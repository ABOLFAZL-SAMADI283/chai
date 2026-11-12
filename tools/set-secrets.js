// ثبت secretها در GitHub با رمزنگاری RSA-OAEP (روش رسمی API)
const PAT = process.argv[2];
const REPO = 'chai';
const OWN = 'ABOLFAZL-SAMADI283';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'setup',
};
const fs = require('fs');
const sodium = require('libsodium-wrappers');

(async () => {
  await sodium.ready;
  // کلید عمومی ریپو (libsodium، ۳۲ بایت base64)
  const pk = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/secrets/public-key`, { headers: H })).json();
  if (!pk || !pk.key) { console.error('public key not found', pk); process.exit(1); }
  console.log('key_id:', pk.key_id);

  const pubKey = Buffer.from(pk.key.trim(), 'base64');
  console.log('pubkey bytes:', pubKey.length);
  const b64 = fs.readFileSync('keystore.b64', 'utf8').trim();

  const secrets = [
    ['KEYSTORE_BASE64', b64],
    ['KEYSTORE_PASS', 'chai2026'],
    ['KEY_ALIAS', 'chai'],
    ['KEY_PASS', 'chai2026'],
  ];

  for (const [name, value] of secrets) {
    // libsodium sealed box
    const sealed = sodium.crypto_box_seal(Buffer.from(value, 'utf8'), pubKey);
    const encrypted = Buffer.from(sealed).toString('base64');

    const r = await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/secrets/${name}`, {
      method: 'PUT',
      headers: { ...H, 'Content-Type': 'application/json' },
      body: JSON.stringify({ encrypted_value: encrypted, key_id: pk.key_id, visibility: 'all' }),
    });
    const t = await r.text();
    console.log(`  secret ${name}: ${r.status} ${r.ok ? '✔' : t.slice(0, 200)}`);
  }

  fs.rmSync('keystore.b64', { force: true });
  console.log('\n✔ همه secretها ثبت شد — بیلد ریلیز امضاشده آمادهٔ اجراست');
})();
