package ir.chai.focus.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/* ─────────────── حرکت‌های مشترک اپ ─────────────── */

/** فنر اصلی — برای تغییر حالت (چیپ، تب، آیکون) */
val ChaiSpring: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = 380f
)

val ChaiSpringDp: AnimationSpec<androidx.compose.ui.unit.Dp> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = 380f
)

/** ورود نرم محتوا */
val ChaiFadeIn = tween<Float>(260)
val ChaiFadeOut = tween<Float>(160)
val ChaiSlow = tween<Float>(700)

/**
 * عددی که با تغییر مقدار، نرم شمارش می‌شود (مثلاً «۲ ساعت و ۱۵ دقیقه»).
 */
@Composable
fun AnimatedNumber(
    value: Int,
    style: TextStyle,
    color: Color = Chai.Text,
    suffix: String = "",
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 700),
        label = "countUp"
    )
    Text(text = "$animated$suffix", style = style, color = color, modifier = modifier)
}
