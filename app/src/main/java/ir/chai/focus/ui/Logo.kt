package ir.chai.focus.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * لوگوی «چای» — همان برند در همه‌جا استفاده می‌شود:
 * اسپلش، صفحهٔ دربارهٔ سازنده، و آیکون اپ.
 *
 * @param fill   درصد پرشدن ماگ (۰ = خالی، ۱ = پر)
 * @param showWord نام «چای» کنار نشان
 */
@Composable
fun ChaiLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    fill: Float = 0f,
    showWord: Boolean = true,
    wordColor: Color = Chai.Text,
    cupColor: Color = Chai.Milk,
    teaColor: Color = Chai.Tea,
    rimColor: Color = Chai.Tea
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val left = w * 0.20f
            val right = w * 0.74f
            val top = h * 0.24f
            val bottom = h * 0.88f

            val body = Path().apply {
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right - (right - left) * 0.12f, bottom)
                quadTo((left + right) / 2f, bottom + h * 0.05f, left + (right - left) * 0.12f, bottom)
                close()
            }

            // دستهٔ ماگ
            drawArc(
                color = rimColor,
                startAngle = -72f, sweepAngle = 144f, useCenter = false,
                topLeft = Offset(right - w * 0.04f, top + h * 0.10f),
                size = Size(w * 0.28f, h * 0.28f),
                style = Stroke(width = w * 0.035f)
            )

            // چای داخل ماگ
            if (fill > 0f) {
                val level = bottom - (bottom - top) * fill.coerceIn(0f, 1f)
                drawPath(body, color = teaColor)
                // سطح چای
                drawRect(
                    color = teaColor.copy(alpha = 0.85f),
                    topLeft = Offset(left, level),
                    size = Size(right - left, 2f)
                )
            }

            // بدنه
            drawPath(body, color = cupColor)
            drawPath(body, color = rimColor, style = Stroke(width = w * 0.030f))
        }

        if (showWord) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "چای",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = wordColor
                )
            }
        }
    }
}

/** لوگوی کوچک برای نوار بالا/پروفایل */
@Composable
fun ChaiLogoMark(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val left = w * 0.20f
        val right = w * 0.74f
        val top = h * 0.24f
        val bottom = h * 0.88f
        val body = Path().apply {
            moveTo(left, top); lineTo(right, top)
            lineTo(right - (right - left) * 0.12f, bottom)
            quadTo((left + right) / 2f, bottom + h * 0.05f, left + (right - left) * 0.12f, bottom)
            close()
        }
        drawArc(
            color = Chai.Tea, startAngle = -72f, sweepAngle = 144f, useCenter = false,
            topLeft = Offset(right - w * 0.04f, top + h * 0.10f),
            size = Size(w * 0.28f, h * 0.28f), style = Stroke(width = w * 0.05f)
        )
        drawPath(body, color = Chai.Milk)
        drawPath(body, color = Chai.Tea, style = Stroke(width = w * 0.05f))
    }
}
