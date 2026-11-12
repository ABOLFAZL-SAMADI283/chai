// ─────────────────────────────────────────────────────────────
//  Edge Function: check-username
//  بررسی آزاد بودن نام کاربری + پیشنهاد نام‌های جایگزین.
//
//  GET /functions/v1/check-username?name=ali
//  → { "available": false, "suggestions": ["ali_2", "aliin", ...] }
//
//  GET /functions/v1/check-username?name=علی&suggest=1
//  → پیشنهاد بر اساس نام فارسی (تبدیل به لاتین + الگوهای رایج)
// ─────────────────────────────────────────────────────────────

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { cors, json, sanitizeUsername, isReserved } from "../_shared/util.ts";

/** تبدیل رایج‌ترین نام‌های فارسی به لاتین (برای پیشنهاد نام کاربری) */
const NAME_MAP: Record<string, string> = {
  "علی": "ali", "علیرضا": "alireza", "محمد": "mohammad", "محمدامین": "mohammadamin",
  "حسین": "hossein", "حسینعلی": "hosseinali", "رضا": "reza", "مهدی": "mahdi",
  "امیر": "amir", "امیرمحمد": "amirmohammad", "میلاد": "milad", "سامان": "saman",
  "سعید": "saeed", "پویا": "pooya", "آرش": "arash", "کاوه": "kaveh",
  "سارا": "sara", "سپیده": "sepideh", "نگین": "negin", "مریم": "maryam",
  "فاطمه": "fateme", "زهرا": "zahra", "مینا": "mina", "نرگس": "narges",
  "الهام": "elham", "شیما": "shima", "پریسا": "parisa", "ترانه": "taraneh",
  "سمیرا": "samira", "هستی": "hasti", "آرزو": "arzoo", "پریسا": "parisa",
  "ایمان": "iman", "میثم": "maysam", "یاسر": "yaser", "مجید": "majid",
  "فرهاد": "farhad", "اشکان": "ashkan", "وحید": "vahid", "شهاب": "shahab",
  "کاوه": "kaveh", "بهار": "bahar", "پاییز": "paeez", "تابستان": "tabestan",
  "یخ": "yakh", "چای": "chai", "دمنوش": "demosh", "کافه": "cafe",
};

const SUFFIXES = ["_", "__", "1", "2", "12", "77", "1404", "1405", "ir", "app", "tea"];

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  try {
    const url = new URL(req.url);
    const raw = url.searchParams.get("name") ?? "";
    const wantSuggestions = url.searchParams.get("suggest") === "1";

    const username = sanitizeUsername(raw);
    if (!username) return json({ error: "نام کاربری باید ۳ تا ۱۲ حرف یا رقم باشد" }, 400);

    const admin = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    );

    const key = username.toLowerCase();

    // آزاد است؟
    const { data: taken } = await admin
      .from("credentials")
      .select("user_id")
      .eq("username", key)
      .maybeSingle();

    const reserved = isReserved(key);
    const available = !taken && !reserved;

    // پیشنهاد نام
    let suggestions: string[] = [];
    if (wantSuggestions || !available) {
      const base = NAME_MAP[raw.trim()] ?? (raw.trim().match(/[a-zA-Z]+/)?.[0]?.toLowerCase() ?? key);
      const candidates = new Set<string>();

      for (const s of SUFFIXES) {
        candidates.add((base + s).slice(0, 12));
        candidates.add((base + "_" + s).slice(0, 12));
      }
      if (base !== key) candidates.add(base.slice(0, 12));

      // پیشنهادهای تصادفی کوتاه
      const alphabet = "abcdefghijklmnopqrstuvwxyz";
      for (let i = 0; i < 6; i++) {
        const rnd = Math.random().toString(36).slice(2, 6);
        candidates.add((base + "_" + rnd).slice(0, 12));
      }

      const list = Array.from(candidates).filter((c) => c.length >= 3 && !isReserved(c));
      const { data: takenAll } = await admin
        .from("credentials")
        .select("username")
        .in("username", list.slice(0, 40));

      const takenSet = new Set((takenAll ?? []).map((r: { username: string }) => r.username));
      suggestions = list.filter((c) => !takenSet.has(c)).slice(0, 8);
    }

    return json({ username: key, available, suggestions });
  } catch (e) {
    return json({ error: String(e) }, 500);
  }
});
