package ir.chai.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.chai.focus.DailyChallenge
import ir.chai.focus.Duel
import ir.chai.focus.FocusSession
import ir.chai.focus.Goal
import ir.chai.focus.LeagueRow
import ir.chai.focus.Leagues
import ir.chai.focus.R
import ir.chai.focus.Stats
import ir.chai.focus.TimeFmt

/* ═══════════════════════ کارت‌های مشترک ═══════════════════════ */

@Composable
fun ChaiCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Chai.Surface)
            .padding(14.dp)
    ) { content() }
}

/** حلقهٔ پیشرفت گرد */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 10.dp,
    trackColor: Color = Chai.Soft,
    progressColor: Color = Chai.Primary,
    center: @Composable () -> Unit = {}
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val r = (size.minDimension - sw) / 2f
            val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            drawArc(
                color = trackColor,
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
            )
        }
        center()
    }
}

/* ═══════════════════════ آمار ═══════════════════════ */

@Composable
fun StatsScreen(
    sessions: List<FocusSession>,
    todayVisits: Int = 0,
    totalVisits: Int = 0,
    modifier: Modifier = Modifier
) {
    val today = Stats.todaySeconds(sessions)
    val week = Stats.last7Days(sessions)
    val streak = Stats.streakDays(sessions)
    val completed = sessions.count { it.completed }
    val dailyGoal = 120 * 60                      // هدف پیش‌فرض روز: ۲ ساعت
    val goalProgress = (today.toFloat() / dailyGoal).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("داشبورد تمرکز", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Chai.Soft)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chai_fire),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Text("$streak روز", style = MaterialTheme.typography.labelMedium, color = Chai.Primary)
                }
            }
        }

        // ── کارت بزرگ امروز ──
        item {
            ChaiCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = goalProgress,
                        modifier = Modifier.size(96.dp),
                        strokeWidth = 9.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${(goalProgress * 100).toInt()}٪",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Chai.Primary
                            )
                            Text("هدف ۲ ساعت", style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("امروز", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                        Text(
                            TimeFmt.humanHours(today),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Chai.Text
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (today >= dailyGoal) "هدف امروز کامل شد 🎉"
                            else "تا هدف امروز ${TimeFmt.humanHours(dailyGoal - today)} مانده",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (today >= dailyGoal) Chai.Primary else Chai.Text2
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("این هفته", TimeFmt.humanHours(week.sumOf { it.second }), Modifier.weight(1f))
                    MiniStat("جلسهٔ کامل", "$completed", Modifier.weight(1f))
                    MiniStat("بازدید امروز", "$todayVisits", Modifier.weight(1f))
                }
            }
        }

        // ── نمودار هفته ──
        item {
            ChaiCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("این هفته", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                    Text(
                        TimeFmt.humanHours(week.sumOf { it.second }),
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Spacer(Modifier.height(12.dp))
                WeeklyBarChart(data = week)
            }
        }

        // ── نقشهٔ حرارتی ──
        item {
            ChaiCard {
                Text("۵ هفتهٔ اخیر", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                val days = remember(sessions) {
                    (0 until 35).map { i ->
                        val ts = System.currentTimeMillis() - (34 - i) * 86400000L
                        sessions.filter { it.dateKey == TimeFmt.dayKey(ts) }.sumOf { it.actualSec } / 60
                    }
                }
                FocusHeatmap(days = days)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("کم", style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
                    Spacer(Modifier.width(4.dp))
                    listOf(
                        Color(0xFFF3EDE3), Color(0xFFD9F2EE), Color(0xFFA5E5DC),
                        Color(0xFF5CC9BA), Chai.Primary
                    ).forEach { c ->
                        Box(
                            Modifier
                                .padding(horizontal = 2.dp)
                                .size(11.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(c)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("زیاد", style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
                }
            }
        }

        // ── تفکیک هدف ──
        if (sessions.isNotEmpty()) {
            item {
                ChaiCard {
                    Text("بر اساس هدف", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                    Spacer(Modifier.height(10.dp))
                    GoalBreakdown(sessions = sessions)
                }
            }
        }

        // ── نشان‌ها ──
        item {
            val badges = computeBadges(sessions, streak)
            ChaiCard {
                Text("نشان‌ها", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                if (badges.isEmpty()) {
                    Text(
                        "هنوز نشانی نگرفته‌ای — یک جلسهٔ تمرکز شروع کن ☕",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                } else {
                    badges.chunked(3).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { b ->
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Chai.Milk)
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(b.emoji, style = MaterialTheme.typography.titleLarge)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        b.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Chai.Text,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        // ── خلاصهٔ کل ──
        item {
            ChaiCard {
                Text("خلاصهٔ کل", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("مجموع تمرکز", TimeFmt.humanHours(sessions.sumOf { it.actualSec }), Modifier.weight(1f))
                    MiniStat("تعداد جلسه", "${sessions.size}", Modifier.weight(1f))
                    MiniStat("کل بازدید", "$totalVisits", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Chai.Milk)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Chai.Teal)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
    }
}

/** نشان‌ها — از روی آمار واقعی */
private data class Badge(val emoji: String, val title: String)

private fun computeBadges(sessions: List<FocusSession>, streak: Int): List<Badge> {
    val out = ArrayList<Badge>()
    val total = sessions.sumOf { it.actualSec }
    val done = sessions.count { it.completed }
    if (sessions.isNotEmpty()) out.add(Badge("🌱", "اولین فنجان"))
    if (total >= 3600) out.add(Badge("⏳", "یک ساعت تمرکز"))
    if (total >= 36000) out.add(Badge("🏆", "ده ساعت تمرکز"))
    if (done >= 10) out.add(Badge("🔥", "ده جلسهٔ کامل"))
    if (done >= 50) out.add(Badge("👑", "پنجاه جلسه"))
    if (streak >= 3) out.add(Badge("📅", "سه روز پیاپی"))
    if (streak >= 7) out.add(Badge("🗓", "یک هفتهٔ کامل"))
    if (streak >= 30) out.add(Badge("⚡", "یک ماه پیوسته"))
    return out
}

/* ═══════════════════════ لیگ ═══════════════════════ */

@Composable
fun LeagueScreen(
    rows: List<LeagueRow>,
    duels: List<Duel> = emptyList(),
    daily: DailyChallenge? = null,
    myLeague: String = "bronze",
    myScore: Int = 0,
    online: Boolean = true,
    onCreateDuel: (String, Int) -> Unit = { _, _ -> },
    onJoinDaily: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDuel by remember { mutableStateOf(false) }
    var duelName by remember { mutableStateOf("") }
    var duelMinutes by remember { mutableStateOf(30) }
    var duelMsg by remember { mutableStateOf<String?>(null) }

    val me = rows.firstOrNull { it.isMe }
    val nextLeague = when (myLeague.lowercase()) {
        "bronze" -> "silver"
        "silver" -> "gold"
        "gold" -> "platinum"
        else -> "platinum"
    }
    val curThreshold = Leagues.threshold(myLeague)
    val nextThreshold = Leagues.threshold(nextLeague)
    val progress = if (nextThreshold <= curThreshold) 1f
    else ((myScore - curThreshold).toFloat() / (nextThreshold - curThreshold)).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!online) {
            item { OfflineNotice("لیگ آفلاین در دسترس نیست — آمارت محلی ذخیره شده") }
        }

        // ── کارت لیگ من ──
        item {
            ChaiCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Leagues.color(myLeague).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Leagues.color(myLeague),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "لیگ ${Leagues.label(myLeague)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Chai.Text
                        )
                        Text(
                            "امتیاز هفتگی: $myScore دقیقه",
                            style = MaterialTheme.typography.bodySmall,
                            color = Chai.Text2
                        )
                    }
                    Text(
                        me?.let { "#${it.rank}" } ?: "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Chai.Primary
                    )
                }
                if (nextLeague != myLeague.lowercase()) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Chai.Soft)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Leagues.color(nextLeague))
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${nextThreshold - myScore} دقیقه تا لیگ ${Leagues.label(nextLeague)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
            }
        }

        // ── جدول ──
        item {
            ChaiCard {
                Text("جدول هفتگی", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                if (rows.isEmpty()) {
                    Text(
                        if (online) "هنوز کسی در این هفته ثبت نشده — بیا اولین نفر باش!"
                        else "برای دیدن جدول، اینترنت لازم است",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                } else {
                    rows.filter { !it.isMe }.take(9).forEach { LeagueRowItem(it) }
                    if (rows.size > 10) {
                        Text(
                            "… و ${rows.size - 10} نفر دیگر",
                            style = MaterialTheme.typography.labelSmall,
                            color = Chai.Text2
                        )
                    }
                }
            }
        }

        // ── جای من ──
        if (me != null) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Chai.Soft)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    LeagueRowItem(me)
                }
            }
        }

        // ── دوئل ──
        item {
            ChaiCard {
                Text("⚔️ دوئل", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                Text(
                    "با یکی از دوستانت رقابت کن؛ هرکدام زودتر به هدف برسد، برنده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
                if (duels.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    duels.take(3).forEach { d ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Chai.Primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${d.hostName} ⚔️ ${d.guestName} — ${d.durationMin} دقیقه",
                                style = MaterialTheme.typography.bodySmall,
                                color = Chai.Text,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (d.finished) "تمام شد" else "باز",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (d.finished) Chai.Text2 else Chai.Primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { showDuel = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Chai.Gold2)
                ) { Text("ساخت دوئل") }
            }
        }

        // ── چالش روزانه ──
        item {
            ChaiCard {
                Text("🎯 چالش روزانه", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                if (daily != null) {
                    Text(
                        "${daily.goal} — ${daily.durationMin} دقیقه",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Chai.Text
                    )
                    Text(
                        "${daily.participants} نفر امروز شرکت کردند",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                } else {
                    Text(
                        "امروز ۳۰ دقیقه تمرکز کن و به بقیه هم انگیزه بده",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onJoinDaily,
                    enabled = online,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (online) Chai.Soft else Chai.Milk,
                        contentColor = if (online) Chai.Primary else Chai.Text2
                    )
                ) { Text("شرکت می‌کنم") }
            }
        }
    }

    if (showDuel) {
        AlertDialog(
            onDismissRequest = { showDuel = false },
            containerColor = Chai.Surface,
            title = { Text("ساخت دوئل", color = Chai.Text) },
            text = {
                Column {
                    OutlinedTextField(
                        value = duelName,
                        onValueChange = { duelName = it },
                        label = { Text("نام کاربری یا نام نمایشی دوستت") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("مدت", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45, 60).forEach { m ->
                            SmallChip(text = "$m دقیقه", selected = duelMinutes == m) {
                                duelMinutes = m
                            }
                        }
                    }
                    if (duelMsg != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            duelMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (duelMsg!!.startsWith("✅")) Chai.Primary else Chai.Danger
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (duelName.isBlank()) {
                            duelMsg = "نام دوستت را بنویس"
                        } else {
                            onCreateDuel(duelName.trim(), duelMinutes)
                            duelMsg = "✅ درخواست دوئل فرستاده شد"
                        }
                    }
                ) { Text("بفرست", color = Chai.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDuel = false; duelMsg = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun LeagueRowItem(r: LeagueRow) {
    val rankColor = when (r.rank) {
        1 -> Chai.Gold
        2 -> Color(0xFF9AA5B1)
        3 -> Color(0xFFB45309)
        else -> Chai.Text2
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            Text(
                "${r.rank}",
                style = MaterialTheme.typography.bodyMedium,
                color = rankColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (r.isMe) Chai.Primary else Chai.Milk),
            contentAlignment = Alignment.Center
        ) {
            Text(r.avatar, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(
                r.nickname + if (r.isMe) " (من)" else "",
                style = MaterialTheme.typography.titleMedium,
                color = if (r.isMe) Chai.Primary else Chai.Text,
                fontWeight = if (r.isMe) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                "${TimeFmt.humanHours(r.minutes * 60)} · ${r.streak} روز",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2
            )
        }
        Text(
            "${r.score}",
            style = MaterialTheme.typography.titleMedium,
            color = if (r.isMe) Chai.Primary else Chai.Text,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ═══════════════════════ بنر آفلاین ═══════════════════════ */

@Composable
fun OfflineNotice(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Chai.Milk)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📴", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = Chai.Text)
    }
}

/* ═══════════════════════ پروفایل ═══════════════════════ */

@Composable
fun ProfileScreen(
    nickname: String,
    username: String,
    avatar: String,
    totalMinutes: Int,
    sessionCount: Int,
    bestStreak: Int,
    savedCode: String = "",
    onOpenSettings: () -> Unit,
    onOpenLegal: (LegalKind) -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Chai.Soft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatar.ifBlank { "☕" }, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    nickname.ifBlank { "مهمان" },
                    style = MaterialTheme.typography.titleLarge,
                    color = Chai.Text
                )
                if (username.isNotBlank()) {
                    Text("@$username", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                } else {
                    Text(
                        "بدون حساب — داده‌ها فقط روی این دستگاه ذخیره می‌شود",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Spacer(Modifier.height(10.dp))
                if (username.isBlank()) {
                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Chai.Primary)
                    ) { Text("ساخت حساب یا ورود") }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("ساعت کل", "$totalMinutes", Modifier.weight(1f))
                StatCard("جلسه", "$sessionCount", Modifier.weight(1f))
                StatCard("بهترین استریک", "$bestStreak", Modifier.weight(1f), Chai.Gold2)
            }
        }

        // ── کد بازیابی ──
        item {
            ChaiCard {
                Text("کد بازیابی", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                Text(
                    "کد بازیابی فقط یک‌بار هنگام ساخت حساب نشان داده می‌شود. اگر ذخیره‌اش کرده‌ای، " +
                        "اینجا نگهش می‌داریم؛ وگرنه باید حساب را از نو بسازی.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Chai.Milk)
                        .padding(12.dp)
                ) {
                    Text(
                        if (savedCode.isBlank()) "••••-••••-••••" else savedCode,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (savedCode.isBlank()) Chai.Text2 else Chai.Tea,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── تنظیمات و قوانین ──
        item {
            ChaiCard {
                ProfileMenuRow(Icons.Filled.Settings, "تنظیمات", "دسترسی‌ها، تم، یادآور", onOpenSettings)
                Divider()
                ProfileMenuRow(null, "قوانین و مقررات", "شرایط استفاده از سرویس") { onOpenLegal(LegalKind.TERMS) }
                Divider()
                ProfileMenuRow(null, "سیاست حریم خصوصی", "چه داده‌ای جمع می‌کنیم") { onOpenLegal(LegalKind.PRIVACY) }
            }
        }

        item {
            Text(
                "خروج از حساب",
                style = MaterialTheme.typography.labelLarge,
                color = Chai.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Chai.Surface)
                    .clickable { onLogout() }
                    .padding(14.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Chai.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Chai.Text)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
        }
        Icon(
            Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = Chai.Text2,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Chai.Line)
    )
}
