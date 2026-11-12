package ir.chai.focus

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * وضعیت دسترسی‌های لازم برای کارکرد «چای».
 *
 * دو دسترسی لازم داریم:
 *  1) Accessibility  → برای قفل کردن اپ‌های حواس‌پرت‌کن
 *  2) Usage access   → برای محاسبهٔ زمان واقعی استفاده از اپ‌ها در داشبورد
 *
 * نکتهٔ مهم (اندروید ۱۱ به بعد): اگر اپ از مرورگر نصب شده باشد، اندروید آن را
 * «Protected app» می‌داند و اجازهٔ فعال‌کردن Accessibility را نمی‌دهد. کاربر باید
 * از تنظیمات، کلید «اجازهٔ تنظیمات محدود» (Allow restricted settings) را روشن کند.
 */
object Permissions {

    /** آیا سرویس Accessibility خودمان فعال است؟ */
    fun accessibilityEnabled(ctx: Context): Boolean {
        val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        return try {
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { it.resolveInfo?.serviceInfo?.packageName == ctx.packageName }
        } catch (e: Exception) {
            false
        }
    }

    /** آیا دسترسی Usage access داده شده؟ */
    fun usageAccessGranted(ctx: Context): Boolean {
        val ops = ctx.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = try {
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myPid(), ctx.packageName)
        } catch (e: Exception) {
            AppOpsManager.MODE_DEFAULT
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /* ─────────────── باز کردن صفحه‌های تنظیمات ─────────────── */

    fun openAccessibilitySettings(ctx: Context) {
        start(ctx, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun openUsageAccessSettings(ctx: Context) {
        val withPackage = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .setData(Uri.fromParts("package", ctx.packageName, null))
        if (!start(ctx, withPackage)) {
            start(ctx, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    /** صفحهٔ «دربارهٔ برنامه» — کلید «اجازهٔ تنظیمات محدود» اینجاست */
    fun openAppDetails(ctx: Context) {
        start(
            ctx,
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", ctx.packageName, null)
            )
        )
    }

    private fun start(ctx: Context, intent: Intent): Boolean = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

/** وضعیت دسترسی‌ها در یک snapshot */
data class PermState(
    val accessibility: Boolean = false,
    val usageAccess: Boolean = false
) {
    val allGranted: Boolean get() = accessibility && usageAccess

    companion object {
        fun read(ctx: Context) = PermState(
            accessibility = Permissions.accessibilityEnabled(ctx),
            usageAccess = Permissions.usageAccessGranted(ctx)
        )
    }
}
