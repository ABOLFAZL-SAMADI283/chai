package ir.chai.focus

import ir.chai.focus.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * همگام‌سازی آفلاین → دیتابیس.
 *
 * الگو: Offline-first
 * ۱) هر جلسه بلافاصله در فایل محلی ذخیره می‌شود (کاربر حتی بدون اینترنت می‌تواند کار کند)
 * ۲) وقتی اینترنت و ورود به حساب آماده بود، جلسه‌های push‌نشده ارسال می‌شوند
 * ۳) بعد از موفقیت، فلگ synced روی آن‌ها می‌خورد تا دوباره ارسال نشوند
 */
class SyncManager(private val store: SessionStore, private val repo: AuthRepository) {

    suspend fun pushPending(): Int = withContext(Dispatchers.IO) {
        if (!repo.isLoggedIn) return@withContext 0

        val all = store.load()
        val pending = all.filter { !it.synced }
        if (pending.isEmpty()) return@withContext 0

        val arr = JSONArray()
        pending.forEach { s ->
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("goal", s.goal)
                    put("planned_sec", s.plannedSec)
                    put("actual_sec", s.actualSec)
                    put("completed", s.completed)
                    put("started_at", java.time.Instant.ofEpochMilli(s.startedAt).toString())
                }
            )
        }

        val result = repo.syncSessions(arr)
        if (result.isSuccess) {
            val okIds = pending.map { it.id }.toSet()
            store.save(all.map { if (it.id in okIds) it.copy(synced = true) else it })
            pending.size
        } else {
            0
        }
    }

    /** جدول رتبه‌بندی هفتگی از سرور */
    suspend fun leaderboardJson(): String? {
        val r = repo.leaderboard()
        return if (r.isSuccess) r.getOrNull() else null
    }
}
