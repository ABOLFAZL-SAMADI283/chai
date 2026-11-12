// ─────────────────────────────────────────────────────────────
//  ابزارهای مشترک Edge Functions: امضای JWT + هش کد بازیابی
// ─────────────────────────────────────────────────────────────

export const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

export function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}

function b64url(bytes: Uint8Array): string {
  let bin = "";
  for (const b of bytes) bin += String.fromCharCode(b);
  return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

/** امضای JWT با HS256 تا auth.uid() در RLS درست کار کند */
export async function signJwt(
  payload: Record<string, unknown>,
  secret: string,
): Promise<string> {
  const enc = new TextEncoder();
  const header = b64url(enc.encode(JSON.stringify({ alg: "HS256", typ: "JWT" })));
  const body = b64url(enc.encode(JSON.stringify(payload)));
  const data = `${header}.${body}`;
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const sig = await crypto.subtle.sign("HMAC", key, enc.encode(data));
  return `${data}.${b64url(new Uint8Array(sig))}`;
}

/** هش SHA-256 کد بازیابی (کد هرگز به‌صورت خام ذخیره نمی‌شود) */
export async function sha256(text: string): Promise<string> {
  const enc = new TextEncoder();
  const digest = await crypto.subtle.digest("SHA-256", enc.encode(text));
  return Array.from(new Uint8Array(digest))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

/** حروف بدون ابهام (بدون 0/O و 1/I/L) */
const ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

/** کد بازیابی ۱۲ کاراکتری با خط جداکننده: XXXX-XXXX-XXXX */
export function generateRecoveryCode(): string {
  const bytes = new Uint8Array(12);
  crypto.getRandomValues(bytes);
  const chars = Array.from(bytes, (b) => ALPHABET[b % ALPHABET.length]);
  return `${chars.slice(0, 4).join("")}-${chars.slice(4, 8).join("")}-${chars
    .slice(8, 12)
    .join("")}`;
}

/** نام کاربری: فقط حرف و عدد انگلیسی/فارسی، ۳ تا ۱۲ کاراکتر */
export function sanitizeUsername(raw: string): string | null {
  const cleaned = raw
    .trim()
    .replace(/[^\p{L}\p{N}_]/gu, "")
    .slice(0, 12);
  return cleaned.length >= 3 ? cleaned : null;
}

export const RESERVED = new Set([
  "admin", "root", "support", "chai", "mod", "system", "null", "undefined",
]);

export function isReserved(username: string): boolean {
  return RESERVED.has(username.toLowerCase());
}
