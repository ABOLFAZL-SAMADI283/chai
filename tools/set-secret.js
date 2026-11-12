// ثبت یک secret در GitHub Actions (رمزنگاری libsodium sealed box)
// استفاده: node tools\set-secret.js <PAT> <NAME> <VALUE>
const PAT = process.argv[2];
const NAME = process.argv[3];
const VALUE = process.argv[4];
const OWN = 'ABOLFAZL-SAMADI283';
const REPO = 'chai';
const H = {
  Authorization: `Bearer ${PAT}`,
  Accept: 'application/vnd.github+json',
  'X-GitHub-Api-Version': '2022-11-28',
  'User-Agent': 'secret',
};

(async () => {
  const sodium = require('libsodium-wrappers');
  await sodium.ready;

  const pk = await (await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/secrets/public-key`, { headers: H })).json();
  const pubKey = Buffer.from(pk.key.trim(), 'base64');
  const sealed = sodium.crypto_box_seal(Buffer.from(VALUE, 'utf8'), pubKey);

  const r = await fetch(`https://api.github.com/repos/${OWN}/${REPO}/actions/secrets/${NAME}`, {
    method: 'PUT',
    headers: { ...H, 'Content-Type': 'application/json' },
    body: JSON.stringify({
      encrypted_value: Buffer.from(sealed).toString('base64'),
      key_id: pk.key_id,
      visibility: 'all',
    }),
  });
  console.log(`secret ${NAME}: ${r.status} ${r.ok ? '✔' : (await r.text()).slice(0, 200)}`);
})();
