// ─────────────────────────────────────────────────────────────
//  Edge Function: login
//  ورود با «نام کاربری + کد بازیابی» (بدون رمز عبور، بدون پیامک)
//
//  کد بازیابی = رمز عبور کاربر در Supabase Auth؛ پس با password grant
//  توکن واقعی می‌گیریم و RLS دیتابیس درست کار می‌کند.
// ─────────────────────────────────────────────────────────────

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { cors, json, sha256, sanitizeUsername } from "../_shared/util.ts";

const URL = Deno.env.get("SUPABASE_URL")!;
const SECRET = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  try {
    const { username: rawName, code } = await req.json();
    const username = sanitizeUsername(String(rawName ?? ""))?.toLowerCase();
    if (!username) return json({ error: "نام کاربری را وارد کنید" }, 400);
    if (!code) return json({ error: "کد بازیابی را وارد کنید" }, 400);

    const admin = createClient(URL, SECRET);

    const { data: cred } = await admin
      .from("credentials")
      .select("user_id, email, recovery_hash")
      .eq("username", username)
      .maybeSingle();

    if (!cred) return json({ error: "حسابی با این نام کاربری نیست" }, 404);

    const normalizedCode = String(code).trim().toUpperCase().replace(/\s/g, "");
    const hash = await sha256(normalizedCode);
    if (hash !== cred.recovery_hash) {
      return json({ error: "کد بازیابی اشتباه است" }, 401);
    }

    // توکن واقعی از Supabase Auth
    const tokenRes = await fetch(`${URL}/auth/v1/token?grant_type=password`, {
      method: "POST",
      headers: { apikey: SECRET, "Content-Type": "application/json" },
      body: JSON.stringify({ email: cred.email, password: normalizedCode }),
    });
    const tokenJson = await tokenRes.json();
    if (!tokenRes.ok || !tokenJson.access_token) {
      return json({ error: "ورود ناموفق بود" }, 401);
    }

    const { data: profile } = await admin
      .from("profiles")
      .select("nickname, avatar")
      .eq("id", cred.user_id)
      .single();

    return json({
      access_token: tokenJson.access_token,
      user_id: cred.user_id,
      username,
      nickname: profile?.nickname ?? username,
      avatar: profile?.avatar ?? "☕",
    });
  } catch (e) {
    return json({ error: String(e) }, 500);
  }
});
