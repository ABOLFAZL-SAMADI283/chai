package ir.chai.focus

import android.content.Context
import android.content.SharedPreferences

/**
 * تنظیمات کاربر — همه‌چیز محلی و بدون حساب کاربری.
 * هر تنظیم واقعاً کار می‌کند (دکمه‌های تنظیمات به این‌ها وصل‌اند).
 */
object Prefs {

    private const val FILE = "chai_settings"

    private fun sp(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /* ───────── صدا و لرزش ───────── */

    /** صدای پایان جلسه */
    fun soundOn(ctx: Context): Boolean = sp(ctx).getBoolean("sound", true)
    fun setSoundOn(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("sound", v).apply()

    /** لرزش پایان جلسه */
    fun vibrateOn(ctx: Context): Boolean = sp(ctx).getBoolean("vibrate", true)
    fun setVibrateOn(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("vibrate", v).apply()

    /* ───────── یادآور روزانه ───────── */

    fun reminderOn(ctx: Context): Boolean = sp(ctx).getBoolean("reminder_on", false)
    fun setReminderOn(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("reminder_on", v).apply()

    fun reminderHour(ctx: Context): Int = sp(ctx).getInt("reminder_h", 8)
    fun setReminderHour(ctx: Context, v: Int) = sp(ctx).edit().putInt("reminder_h", v).apply()

    fun reminderMinute(ctx: Context): Int = sp(ctx).getInt("reminder_m", 0)
    fun setReminderMinute(ctx: Context, v: Int) = sp(ctx).edit().putInt("reminder_m", v).apply()

    /** متن یادآوری سفارشی (خالی = پیش‌فرض) */
    fun reminderText(ctx: Context): String = sp(ctx).getString("reminder_text", "") ?: ""
    fun setReminderText(ctx: Context, v: String) = sp(ctx).edit().putString("reminder_text", v).apply()

    /* ───────── اپ‌های قفل‌شونده ───────── */

    /** بسته‌های اضافه‌ای که کاربر خودش انتخاب کرده */
    fun extraBlocked(ctx: Context): Set<String> =
        sp(ctx).getStringSet("blocked_extra", emptySet()) ?: emptySet()

    fun setExtraBlocked(ctx: Context, set: Set<String>) =
        sp(ctx).edit().putStringSet("blocked_extra", set).apply()

    /* ───────── نمایش ───────── */

    /** نمایش شمارندهٔ «همین الان N نفر فعال هستند» */
    fun showActiveCount(ctx: Context): Boolean = sp(ctx).getBoolean("show_active", true)
    fun setShowActiveCount(ctx: Context, v: Boolean) =
        sp(ctx).edit().putBoolean("show_active", v).apply()

    /* ───────── تمرکز ───────── */

    /** مدت پیش‌فرض جلسه (دقیقه) */
    fun defaultMinutes(ctx: Context): Int = sp(ctx).getInt("default_minutes", 30)
    fun setDefaultMinutes(ctx: Context, v: Int) = sp(ctx).edit().putInt("default_minutes", v).apply()

    /* ───────── حساب ───────── */
    /** آخرین کد بازیابی که کاربر ذخیره کرده (فقط روی همین دستگاه، اختیاری) */
    fun savedRecoveryCode(ctx: Context): String = sp(ctx).getString("recovery_code", "") ?: ""
    fun setSavedRecoveryCode(ctx: Context, v: String) =
        sp(ctx).edit().putString("recovery_code", v).apply()

    /** دیده شدن صفحهٔ قوانین و حریم خصوصی */
    fun legalAccepted(ctx: Context): Boolean = sp(ctx).getBoolean("legal_accepted", false)
    fun setLegalAccepted(ctx: Context, v: Boolean) =
        sp(ctx).edit().putBoolean("legal_accepted", v).apply()

    /* ───────── پاک‌سازی ───────── */

    /** پاک کردن همهٔ داده‌های محلی (جلسات، تنظیمات، حساب) — بدون پاک کردن فایل‌های سیستمی */
    fun wipeAll(ctx: Context) {
        sp(ctx).edit().clear().apply()
        ctx.getSharedPreferences("chai_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        ctx.getSharedPreferences("chai_visits", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
