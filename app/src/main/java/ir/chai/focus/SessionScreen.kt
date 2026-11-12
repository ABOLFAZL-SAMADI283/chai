package ir.chai.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.chai.focus.ui.Chai
import kotlinx.coroutines.delay

/** جلسهٔ فعال */
data class ActiveSession(
    val goal: Goal,
    val totalSec: Int,
    val startedAt: Long = System.currentTimeMillis()
)

@Composable
fun SessionScreen(session: ActiveSession, onExit: (completed: Boolean, elapsedSec: Int) -> Unit) {
    var remaining by remember { mutableIntStateOf(session.totalSec) }
    var askCancel by remember { mutableStateOf(false) }
    val elapsed = session.totalSec - remaining
    val progress by animateFloatAsState(
        targetValue = if (session.totalSec > 0) elapsed.toFloat() / session.totalSec else 0f,
        label = "progress"
    )

    // قفل اپ‌ها
    LaunchedEffect(Unit) {
        FocusBlockerService.sessionActive = true
        FocusBlockerService.blockedPackages = session.goal.packages.toSet()
        FocusBlockerService.goalLabel = session.goal.label
    }

    // شمارش معکوس
    LaunchedEffect(remaining) {
        if (remaining <= 0) {
            FocusBlockerService.sessionActive = false
            onExit(true, elapsed)
            return@LaunchedEffect
        }
        FocusBlockerService.remainingSec = remaining
        delay(1000)
        remaining -= 1
    }

    fun cancel() {
        FocusBlockerService.sessionActive = false
        onExit(false, elapsed)
    }

    Box(modifier = Modifier.fillMaxSize().background(Chai.Bg)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Text(session.goal.label, style = MaterialTheme.typography.bodyLarge, color = Chai.Text2)

            Spacer(Modifier.height(18.dp))
            TeaCup(progress = progress, remainingSec = remaining)

            Spacer(Modifier.height(24.dp))
            // نوار پیشرفت
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Chai.Soft, RoundedCornerShape(99.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(8.dp)
                        .background(Chai.Primary, RoundedCornerShape(99.dp))
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${TimeFmt.humanHours(elapsed)} گذشته",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2
            )

            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Chai.Soft, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_chai_lock),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "اپ‌های حواس‌پرت‌کن قفل شدند",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Primary
                )
            }

            Spacer(Modifier.weight(1f))
            Text(
                "لغو جلسه (استریک می‌شکند)",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2,
                modifier = Modifier.clickable { askCancel = true }.padding(12.dp)
            )
        }
    }

    if (askCancel) {
        AlertDialog(
            onDismissRequest = { askCancel = false },
            title = { Text("لغو جلسه؟") },
            text = { Text("استریک این روز می‌شکند و جلسه نیمه‌کاره ثبت می‌شود.") },
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
 * ماگ چای که به‌مرور پر می‌شود — نمایش پیشرفت به‌جای نوار خشک.
 */
@Composable
fun TeaCup(progress: Float, remainingSec: Int, modifier: Modifier = Modifier) {
    val teaColor = Color(0xFF8B5E3C)
    val cupColor = Color(0xFFF5EFDF)
    val rimColor = Color(0xFF8B5E3C)

    Box(modifier = modifier.size(190.dp), contentAlignment = Alignment.Center) {
        // سایهٔ تیره زیر متن تا روی بخش کرمی و قهوه‌ای لیوان هر دو خوانا بماند
        val timerStyle = MaterialTheme.typography.displaySmall.copy(
            shadow = Shadow(Color(0x99000000), Offset(0f, 2f), 6f)
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val left = w * 0.22f
            val right = w * 0.72f
            val top = h * 0.26f
            val bottom = h * 0.86f

            // بدنهٔ ماگ
            val body = Path().apply {
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right - (right - left) * 0.12f, bottom)
                quadTo((left + right) / 2f, bottom + h * 0.04f, left + (right - left) * 0.12f, bottom)
                close()
            }

            // دسته
            drawArc(
                color = rimColor,
                startAngle = -70f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(right - w * 0.04f, top + h * 0.10f),
                size = Size(w * 0.26f, h * 0.26f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.028f)
            )

            // چای داخل ماگ (با کلیپ)
            clipPath(body) {
                val level = bottom - (bottom - top) * progress.coerceIn(0f, 1f)
                drawRect(
                    color = teaColor,
                    topLeft = Offset(0f, level),
                    size = Size(w, bottom - level + 4f)
                )
            }

            // بدنه
            drawPath(body, color = cupColor)
            drawPath(body, color = rimColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.022f))

            // بخار
            val steam = Path().apply {
                moveTo(w * 0.40f, top - h * 0.06f)
                quadTo(w * 0.46f, top - h * 0.14f, w * 0.40f, top - h * 0.20f)
                moveTo(w * 0.54f, top - h * 0.04f)
                quadTo(w * 0.60f, top - h * 0.12f, w * 0.54f, top - h * 0.18f)
            }
            drawPath(
                steam,
                color = Color(0xFFC9BBA6),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.016f)
            )
        }

        // تایمر: دقیقاً وسط بدنهٔ لیوان (نه وسط کادر).
        // مرکز بدنه ≈ (۴۷٪ عرض، ۵۶٪ ارتفاع) → نسبت به مرکز کادر: ۶dp چپ، ۱۱dp پایین
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (-6).dp, y = 11.dp)
        ) {
            Text(
                TimeFmt.mm:ss(remainingSec),
                style = timerStyle,
                fontWeight = FontWeight.Black,
                color = if (remainingSec < 60) Color(0xFFFF6B6B) else Chai.Milk
            )
            Text(
                "باقی‌مانده",
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(Color(0x99000000), Offset(0f, 1f), 4f)
                ),
                color = Chai.Milk.copy(alpha = 0.9f)
            )
        }
    }
}
