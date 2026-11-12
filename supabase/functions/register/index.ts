// ─────────────────────────────────────────────────────────────
//  Edge Function: register
//  ساخت حساب با «نام کاربری» + صدور «کد بازیابی» یک‌باره.
//
//  نکتهٔ معماری: پروژه‌های جدید Supabase از کلید نامتقارن استفاده می‌کنند
//  و JWT Secret ندارند. پس به‌جای امضای دستی توکن:
//    ۱) با کلید service یک کاربر Auth می‌سازیم که رمزش = کد بازیابی است
//    ۲) با password grant از خود Supabase توکن واقعی می‌گیریم (RLS کار می‌کند)
// ─────────────────────────────────────────────────────────────

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import {
  cors, json, sha256, generateRecoveryCode,
  sanitizeUsername, isReserved,
} from "../_shared/util.ts";

const URL = Deno.env.get("SUPABASE_URL")!;
const SECRET = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  try {
    const { username: rawName, nickname: rawNick } = await req.json();

    const username = sanitizeUsername(String(rawName ?? ""));
    if (!username) return json({ error: "نام کاربری باید ۳ تا ۱۲ حرف یا رقم باشد" }, 400);
    if (isReserved(username)) return json({ error: "این نام کاربری رزرو است" }, 400);

    const nickname = sanitizeUsername(String(rawNick ?? username)) ?? username;
    const avatar = ["☕", "📚", "🌿", "🫖", "🍵", "🌙", "⚡", "🌊"][
      Math.floor(Math.random() * 8)
    ];

    const admin = createClient(URL, SECRET);

    // نام کاربری تکراری؟
    const { data: exists } = await admin
      .from("credentials")
      .select("user_id")
      .eq("username", username.toLowerCase())
      .maybeSingle();
    if (exists) return json({ error: "این نام کاربری قبلاً گرفته شده" }, 409);

    // کد بازیابی = رمز عبور کاربر در Auth (کاربر هیچ‌وقت رمز نمی‌بیند)
    const recovery = generateRecoveryCode();
    const email = `${username.toLowerCase()}@chai-focus.app`;

    const { data: created, error: userErr } = await admin.auth.admin.createUser({
      email,
      password: recovery,
      email_confirm: true,
      user_metadata: { nickname, avatar, username: username.toLowerCase() },
    });
    if (userErr || !created?.user) {
      return json({ error: userErr?.message ?? "ساخت حساب ناموفق بود" }, 400);
    }
    const userId = created.user.id;

    // پروفایل
    const { error: profErr } = await admin.from("profiles").insert({
      id: userId,
      username: username.toLowerCase(),
      nickname,
      avatar,
    });
    if (profErr) {
      await admin.auth.admin.deleteUser(userId);
      return json({ error: profErr.message }, 500);
    }

    // اعتبارنامه
    const { error: credErr } = await admin.from("credentials").insert({
      username: username.toLowerCase(),
      user_id: userId,
      email,
      recovery_hash: await sha256(recovery),
    });
    if (credErr) {
      await admin.auth.admin.deleteUser(userId);
      return json({ error: credErr.message }, 500);
    }

    // گرفتن توکن واقعی از Supabase Auth
    const tokenRes = await fetch(`${URL}/auth/v1/token?grant_type=password`, {
      method: "POST",
      headers: { apikey: SECRET, "Content-Type": "application/json" },
      body: JSON.stringify({ email, password: recovery }),
    });
    const tokenJson = await tokenRes.json();
    if (!tokenRes.ok || !tokenJson.access_token) {
      return json({ error: "دریافت توکن ناموفق بود" }, 500);
    }

    // کد بازیابی فقط همین‌جا، یک‌بار برگردانده می‌شود
    return json({
      access_token: tokenJson.access_token,
      user_id: userId,
      username: username.toLowerCase(),
      nickname,
      avatar,
      recovery_code: recovery,
    });
  } catch (e) {
    return json({ error: String(e) }, 500);
  }
});
