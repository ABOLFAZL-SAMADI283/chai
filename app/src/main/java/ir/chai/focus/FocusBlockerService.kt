package ir.chai.focus

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityEvent
import android.content.Intent
import android.util.Log

/**
 * سرویس قفل اپ‌های حواس‌پرت‌کن.
 * فقط زمانی فعال است که یک جلسهٔ تمرکز فعال باشد.
 */
class FocusBlockerService : AccessibilityService() {

    companion object {
        private const val TAG = "FocusBlocker"

        @Volatile
        var instance: FocusBlockerService? = null

        @Volatile
        var sessionActive: Boolean = false

        @Volatile
        var blockedPackages: Set<String> = emptySet()

        @Volatile
        var remainingSec: Int = 0

        @Volatile
        var goalLabel: String = ""

        fun isConnected(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "accessibility service connected")
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!sessionActive) return
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName) return          // خود اپ مجاز است
        if (pkg !in blockedPackages) return

        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "cannot open block screen: ${e.message}")
        }
    }
}
