package ir.chai.focus

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

/**
 * پایش اتصال اینترنت.
 *
 * هدف: وقتی اینترنت قطع است، **به کاربر بگوییم** — به‌جای اینکه اپ بی‌صدا
 * بی‌فایده به نظر برسد. آمار تمرکز همیشه محلی ذخیره می‌شود، پس قطعی اینترنت
 * کار را متوقف نمی‌کند؛ فقط همگام‌سازی و لیگ انجام نمی‌شود.
 */
class NetMonitor(context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    /** آیا الان اینترنت واقعی (نه فقط Wi-Fi وصل بدون اینترنت) داریم؟ */
    fun isOnline(): Boolean {
        val c = cm ?: return false
        return try {
            val net = c.activeNetwork ?: return false
            val caps = c.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * ثبت شنوندهٔ تغییر وضعیت. تابع برگشتی = لغو ثبت.
     */
    fun observe(onChange: (Boolean) -> Unit): () -> Unit {
        val c = cm ?: return {}
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = onChange(isOnline())
            override fun onLost(network: Network) = onChange(isOnline())
            override fun onUnavailable() = onChange(false)
            override fun onCapabilitiesChanged(
                network: Network,
                caps: NetworkCapabilities
            ) = onChange(isOnline())
        }
        return try {
            c.registerDefaultNetworkCallback(cb)
            {
                runCatching { c.unregisterNetworkCallback(cb) }
                Unit
            }
        } catch (e: Exception) {
            { }
        }
    }
}
