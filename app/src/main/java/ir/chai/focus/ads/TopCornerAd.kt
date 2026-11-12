package ir.chai.focus.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * جای تبلیغ در گوشهٔ بالا.
 *
 * فعلاً چون تبلیغ فعال نیست، ارتفاع صفر می‌گیرد تا فضای اپ خالی و تمیز بماند.
 * با فعال‌شدن تبلیغ (Ads.ENABLED = true) همین‌جا بنر نمایش داده می‌شود.
 */
@Composable
fun TopCornerAd(
    modifier: Modifier = Modifier,
    zoneId: String = Ads.ZONE_BANNER
) {
    if (!Ads.ENABLED) {
        Box(modifier = modifier.fillMaxWidth().height(0.dp))
        return
    }
    Box(modifier = modifier.fillMaxWidth().height(52.dp))
}
