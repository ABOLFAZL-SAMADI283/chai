package ir.chai.focus

import android.content.Context
import java.util.Calendar
import java.util.UUID

/**
 * سیستم بازدید و شمارندهٔ کاربران فعال.
 *
 * دو چیز را نگه می‌دارد:
 *  ۱) بازدیدهای محلی (باز شدن اپ) — بدون سرور، همیشه کار می‌کند
 *  ۲) «همین الان چند نفر مثل من فعال هستند» — از دیتابیس (با کش ۶۰ ثانیه‌ای)
 *
 * هدف: انگیزه. کاربر می‌بیند دیگران هم دارند درس می‌خوانند/می‌خوابند.
 */
class Visits(context: Context) {

    private val prefs = context.getSharedPreferences("chai_visits", Context.MODE_PRIVATE)

    /** شناسهٔ تصادفی دستگاه (بدون هیچ اطلاعات شخصی) */
    val anonId: String
        get() {
            var id = prefs.getString("anon_id", null)
            if (id.isNullOrBlank()) {
                id = UUID.randomUUID().toString()
                prefs.edit().putString("anon_id", id).apply()
            }
            return id!!
        }

    /** بازدید امروز (محلی) */
    val todayOpens: Int
        get() = prefs.getInt("today_$todayKey", 0)

    /** مجموع بازدیدها (محلی) */
    val totalOpens: Int
        get() = prefs.getInt("total", 0)

    /** ثبت یک بازدید */
    fun registerOpen() {
        val e = prefs.edit()
        e.putInt("today_$todayKey", todayOpens + 1)
        e.putInt("total", totalOpens + 1)
        e.apply()
    }

    /** کش شمارندهٔ فعال‌ها */
    fun cachedActive(): Int = prefs.getInt("active_cache", 0)

    fun cacheActive(count: Int, at: Long) {
        prefs.edit().putInt("active_cache", count).putLong("active_at", at).apply()
    }

    fun activeCacheAgeMs(): Long =
        System.currentTimeMillis() - prefs.getLong("active_at", 0L)

    private val todayKey: String
        get() {
            val c = Calendar.getInstance()
            return "%04d-%02d-%02d".format(
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)
            )
        }
}
