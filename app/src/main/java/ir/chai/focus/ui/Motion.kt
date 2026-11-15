package ir.chai.focus.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

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
 * ورود پله‌ای — آیتم‌های یک لیست یکی‌یکی ظاهر می‌شوند.
 * صفحه‌ها را «زنده» می‌کند بدون اینکه کند شود.
 */
@Composable
fun StaggeredItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val dy = remember { Animatable(26f) }
    LaunchedEffect(index) {
        delay((index.coerceIn(0, 12) * 55).toLong())
        alpha.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
        dy.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 260f))
    }
    Box(
        modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = dy.value
        }
    ) { content() }
}

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
