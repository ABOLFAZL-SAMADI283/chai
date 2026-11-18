package ir.chai.focus

import android.content.Context
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
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.chai.focus.ui.Chai
import kotlinx.coroutines.delay

/** جلسهٔ فعال */
data class ActiveSession(
    val goal: Goal,
    val totalSec: Int,
    val startedAt: Long = System.currentTimeMillis()
)

/**
 * نام نمایشی اپ‌هایی که در این جلسه قفل می‌شوند (پیش‌فرض + انتخابیِ کاربر).
 */
fun blockedAppLabels(ctx: Context, packages: Collection<String>): List<String> {
    val pm = ctx.packageManager
    return packages.mapNotNull { pkg ->
        runCatching {
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString().trim()
        }.getOrNull()
    }.filter { it.isNotBlank() }.distinct()
}

@Composable
fun SessionScreen(session: ActiveSession, onExit: (completed: Boolean, elapsedSec: Int) -> Unit) {
    var remaining by remember { mutableIntStateOf(session.totalSec) }
    var askCancel by remember { mutableStateOf(false) }
    var blockerLive by remember { mutableStateOf(FocusBlockerService.isConnected()) }
    var finished by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }
    val elapsed = session.totalSec - remaining
    val ctx = androidx.compose.ui.platform.LocalContext.current

    val progress by animateFloatAsState(
        targetValue = if (session.totalSec > 0) elapsed.toFloat() / session.totalSec else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // قفل اپ‌ها
    val blockedPkgs = remember(session.goal.id) { session.goal.packages + Prefs.extraBlocked(ctx) }
    val blockedNames = remember(blockedPkgs) { blockedAppLabels(ctx, blockedPkgs) }
    val lockText = when {
        blockedNames.isEmpty() -> "اپ‌های حواس‌پرت‌کن قفل شدند"
        blockedNames.size <= 3 -> blockedNames.joinToString("، ") + " قفل شدند"
        else -> blockedNames.take(2).joinToString("، ") + " و ${blockedNames.size - 2} اپ دیگر قفل شدند"
    }

    LaunchedEffect(Unit) {
        FocusBlockerService.sessionActive = true
        FocusBlockerService.blockedPackages = blockedPkgs.toSet()
        FocusBlockerService.goalLabel = session.goal.label
    }

    // شمارش معکوس — با مکث متوقف می‌شود
    LaunchedEffect(remaining, paused) {
        if (paused) return@LaunchedEffect
        if (remaining <= 0) {
            FocusBlockerService.sessionActive = false
            Prefs.setLastSessionSec(ctx, elapsed)
            Feedback.sessionFinished(ctx)      // صدا + لرزش + اعلان (طبق تنظیمات)
            finished = true
            delay(60)
            return@LaunchedEffect
        }
        FocusBlockerService.remainingSec = remaining
        blockerLive = FocusBlockerService.isConnected()
        delay(1000)
        remaining -= 1
    }

    fun cancel() {
        FocusBlockerService.sessionActive = false
        onExit(false, elapsed)
    }

    // پس‌زمینهٔ غوطه‌ور و آرام — مستقل از تم، همیشه تیرهٔ چای
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF12756C), Color(0xFF0A3E3A), Color(0xFF071F1D))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── هدر: هدف + وضعیت ──
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(goalRes(session.goal)),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        session.goal.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFD9F3EE)
                    )
                }
                Text(
                    if (paused) "⏸ مکث" else "☕ در حال دم کشیدن",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9FD8D1)
                )
            }

            Spacer(Modifier.weight(1f))

            // ── لیوان + تایمر ──
            TeaCup(
                progress = progress,
                remainingSec = remaining,
                modifier = Modifier.size(252.dp)
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "${TimeFmt.humanHours(elapsed)} گذشته · ${faDigits("${(progress * 100).toInt()}")}٪",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9FD8D1)
            )

            Spacer(Modifier.height(10.dp))
            // نوار پیشرفت باریک
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF5EEAD4), Color(0xFF2DD4BF))
                            )
                        )
                )
            }

            Spacer(Modifier.height(14.dp))
            // ── وضعیت واقعی سرویس قفل ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_chai_lock),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    if (blockerLive) lockText
                    else "⚠️ سرویس قفل فعال نیست — Accessibility را روشن کن",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (blockerLive) Color(0xFFBFEDE7) else Color(0xFFFFB4B4)
                )
            }

            Spacer(Modifier.weight(1f))

            // ── کنترل‌ها: مکث + پایان ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { paused = !paused },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (paused) "ادامه" else "مکث",
                        tint = Color(0xFFEAFEFB),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box(
                    Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFDE5252).copy(alpha = 0.20f))
                        .clickable { askCancel = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "پایان جلسه",
                        tint = Color(0xFFFF9E9E),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (paused) "مکث زدی — هر وقت آماده بودی ادامه بده"
                else "گوشی رو رها کن — چای داره دم می‌کشه ☕",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7FBDB6),
                textAlign = TextAlign.Center
            )
        }

        // ── پایان جلسه ──
        AnimatedVisibility(
            visible = finished,
            enter = fadeIn(tween(420)) + scaleIn(tween(520), initialScale = 0.86f)
        ) {
            SessionCompleteCard(
                goal = session.goal,
                elapsedSec = elapsed,
                plannedSec = session.totalSec,
                onDone = { onExit(true, elapsed) }
            )
        }
    }

    if (askCancel) {
        AlertDialog(
            onDismissRequest = { askCancel = false },
            title = { Text("لغو جلسه؟", color = Chai.Text) },
            text = {
                Text(
                    "استریک این روز می‌شکند و جلسه نیمه‌کاره ثبت می‌شود.",
                    color = Chai.Text
                )
            },
            confirmButton = {
                TextButton(onClick = { askCancel = false; cancel() }) {
                    Text("بله، لغو کن", color = Chai.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { askCancel = false }) { Text("ادامهٔ تمرکز") }
            },
            containerColor = Chai.Surface
        )
    }
}

/**
 * کارت پایان جلسه — «آفرین!» با خلاصهٔ تمرکز.
 */
@Composable
private fun SessionCompleteCard(
    goal: Goal,
    elapsedSec: Int,
    plannedSec: Int,
    onDone: () -> Unit
) {
    val bubbles = rememberInfiniteTransition(label = "bubbles")
    val t by bubbles.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "bubblePhase"
    )
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f),
        label = "doneScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Chai.Bg.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center
    ) {
        // حباب‌های تزئینی
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(14) { i ->
                val seed = (i * 37 % 100) / 100f
                val x = size.width * ((seed * 1.7f) % 1f)
                val y = size.height * (1f - ((t + seed) % 1f))
                val r = 4f + (i % 4) * 3f
                drawCircle(
                    color = Chai.Primary.copy(alpha = 0.14f),
                    radius = r,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Box(
                Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Chai.Soft),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_chai_cup),
                    contentDescription = null,
                    modifier = Modifier.size(58.dp).scale(scale)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "آفرین! چای تموم شد ☕",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Chai.Teal,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${goal.label} · ${TimeFmt.humanHours(elapsedSec)} تمرکز",
                style = MaterialTheme.typography.bodyMedium,
                color = Chai.Text2
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Chai.Surface)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${faDigits("${if (plannedSec > 0) elapsedSec * 100 / plannedSec else 0}")}٪",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Chai.Primary
                    )
                    Text("کامل", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                }
                Column(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Chai.Surface)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${TimeFmt.humanHours(elapsedSec)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Chai.Teal)
                    Text("تمرکز", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Chai.Primary)
            ) {
                Text("عالی بود، بریم ادامه", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/**
 * ماگ چای که به‌مرور پر می‌شود — با حلقهٔ پیشرفت و بخار.
 */
@Composable
fun TeaCup(
    progress: Float,
    remainingSec: Int,
    modifier: Modifier = Modifier,
    showTimer: Boolean = true,
    accent: Color = Color(0xFF2DD4BF)
) {
    val cupColor = Color(0xFFF5EFDF)
    val rimColor = Color(0xFF8B5E3C)

    val steamAnim = rememberInfiniteTransition(label = "cupSteam")
    val steam by steamAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "steamPhase"
    )
    val breathe by steamAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "breathe"
    )

    Box(
        modifier = modifier.scale(breathe),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // لیوان دقیقاً وسط کادر — مثل موکاپ تأییدشده
            val left = w * 0.235f
            val right = w * 0.765f
            val top = h * 0.267f
            val bottom = h * 0.733f
            val inset = (right - left) * 0.10f
            val cx = (left + right) / 2f
            val cy = (top + bottom) / 2f

            // بدنهٔ ماگ — باریک‌شونده با ته گرد
            val body = Path().apply {
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right - inset, bottom)
                quadraticTo(cx, bottom + h * 0.05f, left + inset, bottom)
                close()
            }

            // سایهٔ نرم زیر ماگ
            drawOval(
                color = Color(0xFF000000).copy(alpha = 0.16f),
                topLeft = Offset(cx - w * 0.30f, bottom + h * 0.035f),
                size = Size(w * 0.60f, h * 0.05f)
            )

            // دستگیره — پشت بدنه
            drawArc(
                color = rimColor,
                startAngle = -70f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(right - w * 0.005f, cy - h * 0.105f),
                size = Size(w * 0.18f, h * 0.21f),
                style = Stroke(width = w * 0.038f)
            )

            // ۱) بدنهٔ ماگ
            drawPath(body, color = cupColor)

            // ۲) چای داخل ماگ (کلیپ‌شده به بدنه)
            clipPath(body) {
                val level = bottom - (bottom - top) * progress.coerceIn(0f, 1f)
                if (bottom - level > 2f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF9C6A42), Color(0xFF7A4E2E)),
                            startY = level,
                            endY = bottom
                        ),
                        topLeft = Offset(0f, level),
                        size = Size(w, bottom - level + 4f)
                    )
                    // سطح چای
                    drawRect(
                        color = Color(0xFFD9A877),
                        topLeft = Offset(0f, level),
                        size = Size(w, 2.4f)
                    )
                }
            }

            // ۳) خط بدنه
            drawPath(body, color = Color.White.copy(alpha = 0.92f), style = Stroke(width = w * 0.024f))

            // ۴) لبهٔ سه‌بعدی لیوان
            drawOval(
                color = Color(0xFFFFFDF8),
                topLeft = Offset(left, top - h * 0.022f),
                size = Size(right - left, h * 0.044f)
            )
            drawOval(
                color = Color(0xFFE8DCC6).copy(alpha = 0.75f),
                topLeft = Offset(left + w * 0.035f, top - h * 0.006f),
                size = Size((right - left) - w * 0.07f, h * 0.022f)
            )

            // براقیت روی بدنه
            drawRect(
                color = Color.White.copy(alpha = 0.34f),
                topLeft = Offset(left + w * 0.045f, top + h * 0.05f),
                size = Size(w * 0.028f, (bottom - top) * 0.6f)
            )

            // بخار
            val steamAlpha = 0.5f + 0.3f * (1f - kotlin.math.abs(steam * 2f - 1f))
            val steamColor = Color(0xFFDCEFEA)
            val p1 = Path().apply {
                moveTo(cx - w * 0.045f, top - h * 0.06f)
                quadraticTo(cx - w * 0.015f, top - h * 0.14f, cx - w * 0.045f, top - h * 0.20f)
                moveTo(cx + w * 0.045f, top - h * 0.05f)
                quadraticTo(cx + w * 0.075f, top - h * 0.13f, cx + w * 0.045f, top - h * 0.19f)
            }
            drawPath(p1, color = steamColor.copy(alpha = steamAlpha), style = Stroke(width = w * 0.016f))

            // حلقهٔ پیشرفت — هم‌مرکز با لیوان (در ۰٪ کشیده نمی‌شود)
            val ringR = w * 0.45f
            drawCircle(
                color = Color.White.copy(alpha = 0.16f),
                radius = ringR,
                center = Offset(cx, cy),
                style = Stroke(width = w * 0.02f)
            )
            if (progress > 0.001f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(cx - ringR, cy - ringR),
                    size = Size(ringR * 2, ringR * 2),
                    style = Stroke(width = w * 0.02f)
                )
            }
        }

        // تایمر — وسط سطح چای
        if (showTimer) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 0.dp, y = 30.dp)
            ) {
                Text(
                    TimeFmt.mmSs(remainingSec),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        // سایهٔ پررنگ تا تایمر روی چای و روی بدنهٔ روشن خوانا بماند
                        shadow = Shadow(Color(0xE6000000), Offset(0f, 2f), 10f)
                    ),
                    fontWeight = FontWeight.Black,
                    color = if (remainingSec < 60) Color(0xFFFF9E9E) else Color(0xFFFFFFFF)
                )
                Text(
                    "باقی‌مانده",
                    style = MaterialTheme.typography.labelSmall.copy(
                        shadow = Shadow(Color(0xCC000000), Offset(0f, 1f), 6f)
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
