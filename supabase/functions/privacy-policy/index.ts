// ─────────────────────────────────────────────────────────────
//  Edge Function: privacy-policy
//  سیاست حریم خصوصی اپ «چای» را به‌صورت HTML سرو می‌کند.
//
//  GET /functions/v1/privacy-policy  →  صفحهٔ سیاست حریم خصوصی (text/html)
//
//  این تابع برای آن است که لینک سیاست حریم خصوصی همیشه در دسترس باشد
//  (گوگل‌پلی، مایکت و کافه‌بازار به یک URL عمومی نیاز دارند).
// ─────────────────────────────────────────────────────────────

const HTML = String.raw`__PRIVACY_HTML__`;

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", {
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "GET, OPTIONS",
        "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
      },
    });
  }

  const headers = {
    "Content-Type": "text/html; charset=utf-8",
    "Cache-Control": "public, max-age=3600",
    "Access-Control-Allow-Origin": "*",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "no-referrer",
  };

  if (req.method === "HEAD") return new Response(null, { headers });

  return new Response(HTML, { status: 200, headers });
});
