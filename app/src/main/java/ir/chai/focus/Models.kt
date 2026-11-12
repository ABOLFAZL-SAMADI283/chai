package ir.chai.focus

import java.util.concurrent.TimeUnit

/** یک جلسهٔ تمرکز ثبت‌شده. همه‌چیز محلی ذخیره می‌شود؛ بدون حساب کاربری. */
data class FocusSession(
    val id: String,
    val goal: String,          // کلید هدف (Goal.id)
    val plannedSec: Int,       // مدت برنامه‌ریزی‌شده
    val actualSec: Int,        // مدت واقعی تمرکز
    val startedAt: Long,       // زمان شروع (epoch millis)
    val completed: Boolean,    // آیا کامل شد (وگرنه جلسه شکست خورد)
    val synced: Boolean = false  // آیا به دیتابیس push شده؟
) {
    val dateKey: String get() = dayKey(startedAt)
}

/** هدف‌های تمرکز — هر هدف لیست پیشنهادی اپ‌های حواس‌پرت‌کن خودش را دارد. */
enum class Goal(
    val id: String,
    val label: String,
    val icon: String,
    val packages: List<String>
) {
    STUDY(
        "study", "درس خواندن", "📚",
        listOf(
            "org.telegram.messenger", "org.telegram.messenger.web",
            "com.instagram.android", "com.google.android.youtube",
            "com.google.android.apps.youtube", "com.zhiliaoapp.musically",
            "com.twitter.android", "com.reddit.frontpage",
            "com.snapchat.android", "com.facebook.katana",
            "com.aparat", "com.google.android.chrome",
            "org.mozilla.firefox", "com.microsoft.emmx"
        )
    ),
    BOOK(
        "book", "کتاب خواندن", "📖",
        listOf(
            "org.telegram.messenger", "org.telegram.messenger.web",
            "com.instagram.android", "com.google.android.youtube",
            "com.google.android.apps.youtube", "com.zhiliaoapp.musically",
            "com.twitter.android", "com.reddit.frontpage",
            "com.google.android.chrome", "org.mozilla.firefox",
            "com.microsoft.emmx", "com.aparat"
        )
    ),
    WORK(
        "work", "کار و پروژه", "💼",
        listOf(
            "org.telegram.messenger", "org.telegram.messenger.web",
            "com.instagram.android", "com.google.android.youtube",
            "com.zhiliaoapp.musically", "com.reddit.frontpage",
            "com.snapchat.android", "com.aparat",
            "com.google.android.chrome", "org.mozilla.firefox"
        )
    ),
    GYM(
        "gym", "ورزش", "🏃",
        listOf(
            "com.google.android.youtube", "com.google.android.apps.youtube",
            "com.instagram.android", "com.zhiliaoapp.musically",
            "com.aparat", "com.google.android.chrome"
        )
    ),
    SLEEP(
        "sleep", "خواب و استراحت", "🌙",
        listOf(
            "org.telegram.messenger", "org.telegram.messenger.web",
            "com.instagram.android", "com.google.android.youtube",
            "com.google.android.apps.youtube", "com.zhiliaoapp.musically",
            "com.twitter.android", "com.reddit.frontpage",
            "com.snapchat.android", "com.aparat",
            "com.google.android.chrome", "org.mozilla.firefox",
            "com.microsoft.emmx", "com.digikala"
        )
    );

    companion object {
        fun byId(id: String): Goal = entries.firstOrNull { it.id == id } ?: STUDY
    }
}

/** اپ‌هایی که همیشه حواس‌پرت‌کن‌اند (پیشنهاد پایه). */
object Distractors {
    val ALWAYS = listOf(
        "org.telegram.messenger", "org.telegram.messenger.web",
        "com.instagram.android", "com.google.android.youtube",
        "com.zhiliaoapp.musically", "com.twitter.android",
        "com.reddit.frontpage", "com.google.android.chrome",
        "org.mozilla.firefox"
    )

    val GAMES = listOf(
        "com.supercell.clashofclans", "com.mojang.minecraftpe",
        "com.roblox.client", "com.dxco.pandavsz", "com.ea.gp.fifaa",
        "com.kiloo.blockblast", "com.supercell.clashroyale",
        "com.nintendo.zaka", "com.mojang.minecraft"
    )
}

object TimeFmt {
    fun mm:ss(totalSec: Int): String {
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }

    fun humanHours(sec: Int): String {
        val h = TimeUnit.SECONDS.toHours(sec.toLong())
        val m = TimeUnit.SECONDS.toMinutes(sec.toLong()) % 60
        return when {
            h > 0 -> "${h} ساعت و $m دقیقه"
            else -> "$m دقیقه"
        }
    }

    /** کلید تاریخ (YYYY-MM-DD) در تایم‌زون محلی */
    fun dayKey(millis: Long): String {
        val c = java.util.Calendar.getInstance()
        c.timeInMillis = millis
        return "%04d-%02d-%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    /** کلید هفته (شنبه تا جمعه) برای نمودار ستونی */
    fun weekKey(millis: Long): String {
        val c = java.util.Calendar.getInstance()
        c.timeInMillis = millis
        c.firstDayOfWeek = java.util.Calendar.SATURDAY
        val day = c.get(java.util.Calendar.DAY_OF_WEEK)
        val offset = (day + 6) % 7   // شنبه = 0
        c.add(java.util.Calendar.DAY_OF_YEAR, -offset)
        return dayKey(c.timeInMillis)
    }
}
