package ir.chai.focus

import org.json.JSONArray
import org.json.JSONObject

/** یک ردیف جدول لیگ هفتگی */
data class LeagueRow(
    val rank: Int,
    val nickname: String,
    val avatar: String,
    val minutes: Int,
    val streak: Int,
    val score: Int,
    val league: String,
    val isMe: Boolean = false
)

/** یک دوئل */
data class Duel(
    val id: String,
    val hostName: String,
    val guestName: String,
    val durationMin: Int,
    val hostSec: Int,
    val guestSec: Int,
    val status: String,
    val createdAt: Long
) {
    val finished: Boolean get() = status == "finished"
}

/** چالش روزانهٔ سرور */
data class DailyChallenge(
    val goal: String,
    val durationMin: Int,
    val participants: Int
)

/** پارس پاسخ‌های سرور به مدل‌های اپ */
object LeagueParser {

    /** جدول لیگ — آرایهٔ JSON از RPC weekly_league */
    fun parseLeague(json: String, myId: String): List<LeagueRow> {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        val rows = ArrayList<LeagueRow>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optString("id")
            rows.add(
                LeagueRow(
                    rank = i + 1,
                    nickname = o.optString("nickname").ifBlank { "کاربر" },
                    avatar = o.optString("avatar").ifBlank { "☕" },
                    minutes = o.optInt("total_sec") / 60,
                    streak = o.optInt("streak"),
                    score = o.optInt("week_score"),
                    league = o.optString("league").ifBlank { "bronze" },
                    isMe = id.isNotBlank() && id == myId
                )
            )
        }
        return rows
    }

    /** دوئل‌های کاربر */
    fun parseDuels(json: String): List<Duel> {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        val out = ArrayList<Duel>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                Duel(
                    id = o.optString("id"),
                    hostName = o.optString("host_name").ifBlank { "—" },
                    guestName = o.optString("guest_name").ifBlank { "—" },
                    durationMin = o.optInt("duration_min", 30),
                    hostSec = o.optInt("host_sec"),
                    guestSec = o.optInt("guest_sec"),
                    status = o.optString("status", "open"),
                    createdAt = o.optString("created_at").let {
                        runCatching {
                            java.time.Instant.parse(it).toEpochMilli()
                        }.getOrDefault(0L)
                    }
                )
            )
        }
        return out
    }

    /** چالش روزانه */
    fun parseDaily(json: String): DailyChallenge? {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return null
        if (arr.length() == 0) return null
        val o = arr.optJSONObject(0) ?: return null
        return DailyChallenge(
            goal = Goal.byId(o.optString("goal")).label,
            durationMin = o.optInt("duration_min", 30),
            participants = o.optInt("participants")
        )
    }
}

/** نام فارسی لیگ‌ها */
object Leagues {
    fun label(key: String): String = when (key.lowercase()) {
        "platinum" -> "پلاتین"
        "gold" -> "طلا"
        "silver" -> "نقره"
        "bronze" -> "برنز"
        else -> "برنز"
    }

    fun color(key: String): androidx.compose.ui.graphics.Color = when (key.lowercase()) {
        "platinum" -> androidx.compose.ui.graphics.Color(0xFF9CA3AF)
        "gold" -> androidx.compose.ui.graphics.Color(0xFFD4A017)
        "silver" -> androidx.compose.ui.graphics.Color(0xFF9AA5B1)
        else -> androidx.compose.ui.graphics.Color(0xFFB45309)
    }

    /** حداقل دقیقه برای هر لیگ */
    fun threshold(key: String): Int = when (key.lowercase()) {
        "platinum" -> 1200
        "gold" -> 600
        "silver" -> 240
        else -> 0
    }
}
