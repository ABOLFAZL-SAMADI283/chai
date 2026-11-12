package ir.chai.focus.ads

import android.content.Context

/**
 * تبلیغات — فعلاً غیرفعال.
 *
 * چرا فعلاً؟ قبل از اضافه‌شدن تبلیغ باید تصمیم بگیریم:
 *  - کدام شبکه (تپسل / ادیوری / تبريزياد)
 *  - کجای اپ (بنر گوشهٔ بالا / بین جلسه‌ها)
 *  - آیا «حذف تبلیغ» خریدنی باشد (IAP مایکت)
 *
 * ✅ سیستم «بازدید» (Visits) آماده‌ست تا بدون تبلیغ هم آمار واقعی کاربر داشته باشیم.
 *
 * ── راهنمای فعال‌سازی (۳ قدم) ─────────────────────────────────
 * ۱) در app/build.gradle.kts:
 *      implementation("ir.tapsell:tapsell:1.0.2-beta08")
 *      implementation("ir.tapsell.mediation.adapter:legacy:1.0.2-beta08")
 *    و در settings.gradle.kts ریپوی maven.tapsell.ir را برگردان.
 * ۲) در AndroidManifest مجوز AD_ID را اضافه کن:
 *      <uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
 * ۳) در AdManager.requestBanner بنر را به کانتینر اضافه کن و
 *    Tapsell.requestBannerAd(zoneId, BannerSize.BANNER_320_50, ...) را صدا بزن.
 */
object Ads {
    /** تا وقتی کلید و شبکه انتخاب نشده، همه‌چیز خاموش است */
    const val ENABLED = false

    const val ZONE_BANNER = "YOUR_BANNER_ZONE"
    const val ZONE_INTERSTITIAL = "YOUR_INTERSTITIAL_ZONE"
    const val ZONE_REWARDED = "YOUR_REWARDED_ZONE"

    fun requestBanner(context: Context, zoneId: String = ZONE_BANNER) {
        // عمداً خالی — بعد از انتخاب شبکه پر می‌شود
    }

    fun showInterstitial(context: Context, zoneId: String = ZONE_INTERSTITIAL) {
        // عمداً خالی
    }

    fun showRewarded(context: Context, onReward: () -> Unit, zoneId: String = ZONE_REWARDED) {
        // عمداً خالی
    }
}
