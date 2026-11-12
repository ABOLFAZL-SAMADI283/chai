package ir.chai.focus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.chai.focus.SessionStore
import ir.chai.focus.Stats

/* ───────────────── نمودار ستونی هفته ───────────────── */

@Composable
fun WeeklyBarChart(
    data: List<Pair<String, Int>>,   // (کلید روز، ثانیه)
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 96.dp
) {
    val max = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.BottomStart) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
                val n = data.size
                if (n == 0) return@Canvas
                val gap = size.width / n * 0.28f
                val barW = size.width / n - gap
                data.forEachIndexed { i, (_, sec) ->
                    val ratio = sec.toFloat() / max
                    val barH = (size.height * ratio).coerceAtLeast(if (sec > 0) 6f else 2f)
                    val x = i * (barW + gap)
                    val isToday = i == n - 1
                    drawRoundRect(
                        color = if (isToday) Chai.Primary else Chai.Soft,
                        topLeft = Offset(x, size.height - barH),
                        size = Size(barW, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { d ->
                Text(
                    d,
                    style = MaterialTheme.typography.labelSmall,
                    color = Chai.Text2,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/* ───────────────── heatmap تقویمی ───────────────── */

@Composable
fun FocusHeatmap(
    days: List<Int>,                // دقیقهٔ تمرکز هر روز (از قدیم به جدید)
    modifier: Modifier = Modifier,
    cellSize: androidx.compose.ui.unit.Dp = 16.dp
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        days.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { min ->
                    val color = when {
                        min <= 0 -> Color(0xFFF3EDE3)
                        min < 30 -> Color(0xFFD9F2EE)
                        min < 60 -> Color(0xFFA5E5DC)
                        min < 120 -> Color(0xFF5CC9BA)
                        else -> Chai.Primary
                    }
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
                repeat(7 - week.size) { Spacer(Modifier.size(cellSize)) }
            }
        }
    }
}

/* ───────────────── تفکیک بر اساس هدف ───────────────── */

@Composable
fun GoalBreakdown(
    sessions: List<ir.chai.focus.FocusSession>,
    modifier: Modifier = Modifier
) {
    val byGoal = Stats.byGoal(sessions)
    val total = byGoal.values.sum().coerceAtLeast(1)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        byGoal.entries.sortedByDescending { it.value }.forEach { (goalId, sec) ->
            val goal = ir.chai.focus.Goal.byId(goalId)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(goal.label, style = MaterialTheme.typography.bodySmall, color = Chai.Text)
                    Text(
                        "${(sec * 100 / total)}٪",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Chai.Soft)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(sec.toFloat() / total)
                            .height(7.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Chai.Primary)
                    )
                }
            }
        }
    }
}

/* ───────────────── کارت آمار ───────────────── */

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Chai.Text
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Chai.Surface)
            .padding(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = accent
        )
    }
}
