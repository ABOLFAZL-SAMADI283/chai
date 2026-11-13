package ir.chai.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.chai.focus.ui.Chai.Bg
import ir.chai.focus.ui.Chai.Milk
import ir.chai.focus.ui.Chai.Soft
import ir.chai.focus.ui.Chai.Tea
import ir.chai.focus.ui.Chai.Teal
import ir.chai.focus.ui.Chai.Text2
import kotlinx.coroutines.delay

/**
 * اسپلش «چای» — نسخهٔ کامل:
 *  • پس‌زمینهٔ گرادیانی نرم
 *  • حلقهٔ در حال چرخش دور ماگ
 *  • بخار که بالا می‌رود
 *  • ماگ که آرام‌آرام پر می‌شود
 *  • لوگو و شعار با فنر نرم ظاهر می‌شوند
 *  • محو شدن نرم
 */
@Composable
fun ChaiSplashScreen(
    onFinished: () -> Unit,
    appName: String = "چای"
) {
    var phase by remember { mutableStateOf(0) }   // ۰ = آماده، ۱ = پر شدن، ۲ = محو
    var visible by remember { mutableStateOf(true) }

    val fill by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "cupFill"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0.78f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 220f),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "logoAlpha"
    )
    val tagAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 350),
        label = "tagAlpha"
    )
    val screenAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "screenAlpha"
    )
    val ringSpin by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5200, easing = androidx.compose.animation.core.LinearEasing)),
        label = "ringSpin"
    )
    val steam by rememberInfiniteTransition(label = "steam").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "steamPhase"
    )

    LaunchedEffect(Unit) {
        delay(250)
        phase = 1
        delay(1750)
        phase = 2
        delay(560)
        visible = false
        onFinished()
    }

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Chai.Bg, Soft.copy(alpha = 0.55f), Chai.Bg)
                    )
                )
                .alpha(screenAlpha),
            contentAlignment = Alignment.Center
        ) {
            // ── حلقهٔ چرخان + بخار ──
            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension * 0.46f

                    // حلقهٔ نقطه‌ای چرخان
                    val dotR = r
                    repeat(10) { i ->
                        val a = Math.toRadians((ringSpin + i * 36f).toDouble())
                        val x = cx + (dotR * kotlin.math.cos(a)).toFloat()
                        val y = cy + (dotR * kotlin.math.sin(a)).toFloat()
                        val fade = 0.12f + 0.32f * (1f - i / 10f)
                        drawCircle(color = Tea.copy(alpha = fade), radius = 3.2f, center = Offset(x, y))
                    }

                    // بخار: دو خط موج بالای ماگ
                    val s = steam
                    repeat(2) { k ->
                        val baseX = cx - 18f + k * 36f
                        val path = Path()
                        val top = cy - r - 26f * s
                        val bottom = cy - r + 4f
                        path.moveTo(baseX, bottom)
                        path.quadraticTo(
                            baseX + (if (k == 0) 12f else -12f) * s,
                            (top + bottom) / 2f,
                            baseX, top
                        )
                        drawPath(
                            path,
                            color = Color(0xFFC9BBA6).copy(alpha = 0.45f * (1f - s)),
                            style = Stroke(width = 3.2f)
                        )
                    }
                }
            }

            // ── ماگ + لوگو ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-16).dp)
            ) {
                Box(
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(logoAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    ChaiLogo(
                        modifier = Modifier.size(190.dp),
                        fill = fill,
                        showWord = false,
                        cupColor = Milk,
                        teaColor = Tea,
                        rimColor = Tea
                    )
                }

                Spacer(Modifier.height(14.dp))

                AnimatedVisibility(
                    visible = phase >= 1,
                    enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.9f)
                ) {
                    Text(
                        appName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Teal
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    "۳۰ دقیقه تمرکز، یک ماگ چای",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Text2,
                    modifier = Modifier.alpha(tagAlpha)
                )
            }
        }
    }
}
