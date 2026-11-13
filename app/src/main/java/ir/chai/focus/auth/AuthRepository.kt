package ir.chai.focus.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

/**
 * لایهٔ احراز هویت «چای».
 *
 * مدل: **نام کاربری + کد بازیابی** — بدون رمز عبور، بدون پیامک، بدون ایمیل اجباری.
 * کد بازیابی یک‌بار هنگام ثبت‌نام نشان داده می‌شود و کاربر خودش ذخیره می‌کند.
 * توکن (JWT) در SharedPreferences نگه داشته می‌شود.
 */
class AuthRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("chai_prefs", Context.MODE_PRIVATE)

    val baseUrl: String
        get() = prefs.getString("supabase_url", null) ?: SupabaseConfig.URL
    val anonKey: String
        get() = prefs.getString("supabase_anon_key", null) ?: SupabaseConfig.PUBLISHABLE_KEY

    val isLoggedIn: Boolean get() = !prefs.getString("access_token", "").isNullOrBlank()
    val username: String get() = prefs.getString("username", "") ?: ""
    val nickname: String get() = prefs.getString("nickname", "") ?: ""
    val avatar: String get() = prefs.getString("avatar", "☕") ?: "☕"
    val userId: String get() = prefs.getString("user_id", "") ?: ""

    fun saveConfig(url: String, anon: String) {
        prefs.edit().putString("supabase_url", url).putString("supabase_anon_key", anon).apply()
    }

    fun saveSession(token: String, userId: String, username: String, nickname: String, avatar: String) {
        prefs.edit()
            .putString("access_token", token)
            .putString("user_id", userId)
            .putString("username", username)
            .putString("nickname", nickname)
            .putString("avatar", avatar)
            .apply()
    }

    fun clearSession() {
        prefs.edit().remove("access_token").remove("user_id").apply()
    }

    /**
     * ثبت‌نام: نام کاربری می‌سازد و کد بازیابی برمی‌گرداند (فقط همین یک‌بار).
     * خروجی: RecoveryResult با کد بازیابی برای نمایش به کاربر.
     */
    suspend fun register(username: String, nickname: String): Result<RecoveryResult> =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject()
                    .put("username", username)
                    .put("nickname", nickname.ifBlank { username })
                    .toString()
                val res = request("functions/v1/${fnPath("register")}", "POST", body, authed = false)
                if (!res.first) return@withContext failure(res.second)

                val o = JSONObject(res.second)
                saveSession(
                    o.optString("access_token"),
                    o.optString("user_id"),
                    o.optString("username"),
                    o.optString("nickname"),
                    o.optString("avatar")
                )
                Result.success(
                    RecoveryResult(
                        code = o.optString("recovery_code"),
                        username = o.optString("username"),
                        nickname = o.optString("nickname")
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** ورود با نام کاربری + کد بازیابی */
    suspend fun login(username: String, code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("username", username).put("code", code).toString()
            val res = request("functions/v1/${fnPath("login")}", "POST", body, authed = false)
            if (!res.first) return@withContext failure(res.second)

            val o = JSONObject(res.second)
            saveSession(
                o.optString("access_token"),
                o.optString("user_id"),
                o.optString("username"),
                o.optString("nickname"),
                o.optString("avatar")
            )
            Result.success(o.optString("username"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** بررسی آزاد بودن نام کاربری + دریافت پیشنهاد جایگزین */
    suspend fun checkUsername(name: String, wantSuggestions: Boolean): Result<UsernameStatus> =
        withContext(Dispatchers.IO) {
            try {
                val q = "?name=" + java.net.URLEncoder.encode(name, "UTF-8") +
                    (if (wantSuggestions) "&suggest=1" else "")
                val res = request("functions/v1/${fnPath("check-username")}$q", "GET", "{}", authed = false)
                if (!res.first) return@withContext failure(res.second)
                val o = JSONObject(res.second)
                Result.success(
                    UsernameStatus(
                        available = o.optBoolean("available"),
                        suggestions = o.optJSONArray("suggestions")?.let { arr ->
                            (0 until arr.length()).map { arr.optString(it) }
                        } ?: emptyList()
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** ارسال سشن‌های محلی (کش آفلاین) به دیتابیس */
    suspend fun syncSessions(sessions: JSONArray): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val res = request(
                "rest/v1/sessions", "POST", sessions.toString(), authed = true,
                extraHeaders = mapOf("Prefer" to "resolution=ignore-duplicates")
            )
            // ۴۰۹ = این سشن قبلاً فرستاده شده → عملاً موفق
            if (res.first || res.second.contains("23505")) Result.success(sessions.length())
            else Result.failure(Exception(res.second))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** جدول لیگ هفتگی (RPC) */
    suspend fun league(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = request("rest/v1/rpc/weekly_league", "POST", "{}", authed = true)
            if (res.first) Result.success(res.second) else Result.failure(Exception(res.second))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** دوئل‌های من */
    suspend fun myDuels(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = request("rest/v1/rpc/my_duels", "POST", "{}", authed = true)
            if (res.first) Result.success(res.second) else Result.failure(Exception(res.second))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** ساخت دوئل با نام کاربر یا نام نمایشی */
    suspend fun createDuel(guestName: String, minutes: Int): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject()
                    .put("p_guest", guestName)
                    .put("p_duration_min", minutes)
                    .toString()
                val res = request("rest/v1/rpc/create_duel", "POST", body, authed = true)
                if (res.first) Result.success(res.second) else Result.failure(Exception(res.second))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** چالش روزانه */
    suspend fun dailyChallenge(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = request("rest/v1/rpc/daily_challenge", "POST", "{}", authed = false)
            if (res.first) Result.success(res.second) else Result.failure(Exception(res.second))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** شرکت در چالش روزانه — تعداد شرکت‌کنندگان را برمی‌گرداند */
    suspend fun joinDailyChallenge(goal: String, minutes: Int): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject()
                    .put("p_goal", goal)
                    .put("p_minutes", minutes)
                    .toString()
                val res = request("rest/v1/rpc/join_daily_challenge", "POST", body, authed = true)
                if (res.first) Result.success(res.second.trim().toIntOrNull() ?: 0)
                else Result.failure(Exception(res.second))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * مسیر واقعی تابع Edge.
     * اگر اسلاگ توابع UUID باشد (حالت API)، از جدول function_routes خوانده می‌شود
     * و کش می‌شود تا اپ به اینترنت وابسته نباشد.
     */
    private fun fnPath(name: String): String {
        val slug = prefs.getString("fn_$name", null)
        return if (slug.isNullOrBlank()) name else slug
    }

    /** به‌روزرسانی کش مسیر توابع از دیتابیس */
    suspend fun refreshFunctionRoutes(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = request("rest/v1/function_routes?select=name,slug", "GET", "", authed = false)
            if (!res.first) return@withContext Result.failure(Exception(res.second))
            val arr = org.json.JSONArray(res.second)
            val e = prefs.edit()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                e.putString("fn_${o.optString("name")}", o.optString("slug"))
            }
            e.apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** ثبت بازدید + heartbeat (برای شمارندهٔ «همین الان چند نفر فعال هستند») */
    suspend fun logVisit(anonId: String, goal: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("p_anon", anonId).put("p_goal", goal).toString()
            val res = request("rest/v1/rpc/log_visit", "POST", body, authed = false)
            if (res.first) Result.success(Unit) else Result.failure(Exception(res.second))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** چند نفر همین الان فعال هستند + بازدید امروز */
    suspend fun activeCount(): Result<ActiveCount> = withContext(Dispatchers.IO) {
        try {
            val res = request("rest/v1/rpc/active_count", "POST", "{}", authed = false)
            if (!res.first) return@withContext Result.failure(Exception(res.second))
            val o = JSONObject(res.second)
            val byGoal = HashMap<String, Int>()
            val g = o.optJSONObject("by_goal")
            if (g != null) {
                g.keys().forEach { k -> byGoal[k] = g.optInt(k) }
            }
            Result.success(
                ActiveCount(
                    total = o.optInt("total"),
                    byGoal = byGoal,
                    todayOpens = o.optInt("today_opens")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** جدول رتبه‌بندی هفتگی */
    suspend fun leaderboard(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = request("rest/v1/rpc/weekly_league", "POST", "{}", authed = true)
            if (res.first) Result.success(res.second) else Result.failure(Exception(res.second))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun failure(raw: String): Result<Nothing> {
        val msg = try {
            JSONObject(raw).optString("error", "خطای نامشخص")
        } catch (e: Exception) {
            "ارتباط با سرور برقرار نشد"
        }
        return Result.failure(Exception(msg))
    }

    private fun request(
        path: String,
        method: String,
        body: String,
        authed: Boolean,
        extraHeaders: Map<String, String> = emptyMap()
    ): Pair<Boolean, String> {
        if (baseUrl.isBlank()) return false to """{"error":"سرور تنظیم نشده"}"""
        return try {
            val conn = URL("$baseUrl/$path").openConnection() as HttpURLConnection
            conn.requestMethod = method
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("apikey", anonKey)
            extraHeaders.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            if (authed) {
                val token = prefs.getString("access_token", "") ?: ""
                if (token.isBlank()) return false to """{"error":"وارد نشده‌اید"}"""
                conn.setRequestProperty("Authorization", "Bearer $token")
            }
            if (method != "GET" && method != "HEAD") {
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toByteArray()) }
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
            (code in 200..299) to text
        } catch (e: Exception) {
            false to """{"error":"ارتباط با سرور برقرار نشد"}"""
        }
    }
}

/** نتیجهٔ ثبت‌نام: کد بازیابی که فقط همین یک‌بار نمایش داده می‌شود */
data class RecoveryResult(
    val code: String,
    val username: String,
    val nickname: String
)

/** وضعیت آزاد بودن نام کاربری + پیشنهادهای جایگزین */
data class UsernameStatus(
    val available: Boolean,
    val suggestions: List<String>
)

/** شمارندهٔ کاربران فعال */
data class ActiveCount(
    val total: Int,
    val byGoal: Map<String, Int>,
    val todayOpens: Int
)
