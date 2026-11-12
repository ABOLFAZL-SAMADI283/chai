package ir.chai.focus

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * همهٔ داده‌ها در **یک فایل محلی** ذخیره می‌شود (بدون حساب کاربری، بدون سرور).
 * مسیر: filesDir/sessions.json  → در بکاپ اندروید هم دیده می‌شود.
 */
class SessionStore(context: Context) {

    private val file = File(context.filesDir, "sessions.json")

    fun load(): List<FocusSession> {
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                FocusSession(
                    id = o.optString("id"),
                    goal = o.optString("goal"),
                    plannedSec = o.optInt("plannedSec"),
                    actualSec = o.optInt("actualSec"),
                    startedAt = o.optLong("startedAt"),
                    completed = o.optBoolean("completed"),
                    synced = o.optBoolean("synced")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(sessions: List<FocusSession>) {
        val arr = JSONArray()
        sessions.forEach { s ->
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("goal", s.goal)
                    put("plannedSec", s.plannedSec)
                    put("actualSec", s.actualSec)
                    put("startedAt", s.startedAt)
                    put("completed", s.completed)
                    put("synced", s.synced)
                }
            )
        }
        file.writeText(arr.toString())
    }

    fun add(session: FocusSession): List<FocusSession> {
        val all = load() + session
        save(all)
        return all
    }

    /** خروجی متنی برای اشتراک‌گذاری یا پشتیبان‌گیری دستی */
    fun exportText(sessions: List<FocusSession> = load()): String {
        val sb = StringBuilder()
        sb.append("گزارش تمرکز — چای\n")
        sb.append("───────────────\n")
        sessions.sortedByDescending { it.startedAt }.forEach { s ->
            val g = Goal.byId(s.goal).label
            val status = if (s.completed) "✅ کامل" else "⛔ نیمه‌کاره"
            sb.append("${TimeFmt.dayKey(s.startedAt)}  $g  ${TimeFmt.humanHours(s.actualSec)}  $status\n")
        }
        sb.append("───────────────\n")
        sb.append("مجموع: ${TimeFmt.humanHours(sessions.sumOf { it.actualSec })}\n")
        return sb.toString()
    }
}

/** محاسبات آماری روی همان لیست محلی */
object Stats {

    fun todaySeconds(sessions: List<FocusSession>): Int {
        val today = TimeFmt.dayKey(System.currentTimeMillis())
        return sessions.filter { it.dateKey == today }.sumOf { it.actualSec }
    }

    /** مجموع ۷ روز اخیر */
    fun last7Days(sessions: List<FocusSession>): List<Pair<String, Int>> {
        val c = java.util.Calendar.getInstance()
        val out = ArrayList<Pair<String, Int>>()
        for (i in 6 downTo 0) {
            c.timeInMillis = System.currentTimeMillis()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val key = TimeFmt.dayKey(c.timeInMillis)
            val sec = sessions.filter { it.dateKey == key }.sumOf { it.actualSec }
            out.add(key to sec)
        }
        return out
    }

    /** نگاشت هفته‌ها برای نمودار ستونی (۸ هفتهٔ اخیر) */
    fun weekly(sessions: List<FocusSession>): List<Pair<String, Int>> {
        val c = java.util.Calendar.getInstance()
        val out = ArrayList<Pair<String, Int>>()
        for (i in 7 downTo 0) {
            c.timeInMillis = System.currentTimeMillis()
            c.add(java.util.Calendar.WEEK_OF_YEAR, -i)
            c.firstDayOfWeek = java.util.Calendar.SATURDAY
            val key = TimeFmt.weekKey(c.timeInMillis)
            val sec = sessions.filter { TimeFmt.weekKey(it.startedAt) == key }.sumOf { it.actualSec }
            out.add(key to sec)
        }
        return out
    }

    /** روزهایی که حداقل یک جلسهٔ کامل داشته‌اند (برای streak) */
    fun streakDays(sessions: List<FocusSession>): Int {
        if (sessions.isEmpty()) return 0
        val doneDays = sessions.filter { it.completed && it.actualSec >= 60 }
            .map { it.dateKey }
            .toSet()
        if (doneDays.isEmpty()) return 0

        var streak = 0
        val c = java.util.Calendar.getInstance()
        // اگر امروز هنوز جلسه‌ای کامل نشده، از دیروز شروع می‌کنیم
        if (!doneDays.contains(TimeFmt.dayKey(c.timeInMillis))) {
            c.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        while (true) {
            val key = TimeFmt.dayKey(c.timeInMillis)
            if (!doneDays.contains(key)) break
            streak++
            c.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    /** مجموع بر اساس هدف */
    fun byGoal(sessions: List<FocusSession>): Map<String, Int> =
        sessions.groupBy { it.goal }
            .mapValues { (_, v) -> v.sumOf { it.actualSec } }
}
